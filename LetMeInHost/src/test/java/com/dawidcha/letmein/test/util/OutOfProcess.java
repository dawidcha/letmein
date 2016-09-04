package com.dawidcha.letmein.test.util;

import com.dawidcha.letmein.test.fixtures.Fixture;
import com.dawidcha.letmein.util.Fn;
import com.dawidcha.letmein.util.Holder;
import com.dawidcha.letmein.util.Pair;

import java.io.*;
import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class OutOfProcess {
    private final boolean showOutput;
    private final Fixture fixture;

    private StreamReader stdout;
    private StreamReader stderr;

    private Process process;

    public class StreamReader implements Runnable {
        private final LineNumberReader rdr;
        public final LinkedBlockingDeque<String> lines = new LinkedBlockingDeque<>();
        private final PrintStream out;

        public StreamReader(InputStream in, PrintStream out) {
            this.rdr = new LineNumberReader(new BufferedReader(new InputStreamReader(in)));
            this.out = out;
            new Thread(this).start();
        }

        public void run() {
            while(!Thread.currentThread().isInterrupted()) {
                try {
                    String line= rdr.readLine();
                    if (line == null) {
                        break;
                    }
                    lines.put(line);
                    if(out != null) {
                        out.println(">>> " + fixture.getName() + " <<<: " + line);
                    }
                }
                catch(IOException e) {
                    break;
                }
                catch(InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public OutOfProcess(Fixture fixture, boolean showOutput) {
        this.fixture = fixture;
        this.showOutput = showOutput;
    }

    public void processStarted(Process process) {
        this.process = process;
        this.stdout = process == null? null: new StreamReader(process.getInputStream(), showOutput? System.out: null);
        this.stderr = process == null? null: new StreamReader(process.getErrorStream(), showOutput? System.err: null);
    }

    public void drainOutput(Collection<String> drainStdout, Collection<String> drainStderr) {
        if(stdout != null) {
            stdout.lines.drainTo(drainStdout);
        }
        if(stderr != null) {
            stderr.lines.drainTo(drainStderr);
        }
    }

    public void killProcess(Runnable shutdown) throws Exception {
        if(process != null) {
            System.out.println("Stopping " + fixture.getName());

            Holder<Boolean> done = new Holder<>(false);
            while(!done.value) {
                if(shutdown != null) {
                    shutdown.run();
                }
                else {
                    process.destroy();
                }
                Fn.check(() -> {
                    if(process.waitFor(60, TimeUnit.SECONDS)) {
                        done.value = true;
                    }
                    else {
                        System.out.println("Process '" + fixture.getName() + "' failed to shut down cleanly");
                    }
                });
                shutdown = null;
            }

        }
        process = null;
    }

    public Process getProcess() {
        return process;
    }

    public void showOutput(List<String> out, List<String> err) {
        if(out.size() > 0) {
            System.out.println("_______________________________/--- " + fixture.getName() + " stdout ---\\_______________________________");
            out.forEach(System.out::println);
        }
        if(err.size() > 0) {
            System.err.println("_______________________________/--- " + fixture.getName() + " stderr ---\\_______________________________");
            err.forEach(System.err::println);
        }
        System.out.println("===============================\\___ " + fixture.getName() + " done   ___/===============================");
    }

    /**
     * Wait to see a single message in the log
     * @param timeoutMs how long to wait
     * @param pattern the pattern to look for
     * @param rewind true to rewind messages, false to consume
     * @return a list of matched groups
     * @throws IOException
     */
    public List<String> waitForMessage(long timeoutMs, Pattern pattern, boolean rewind) throws IOException {
        return waitForMessages(timeoutMs, new Pattern[] { pattern }, rewind).iterator().next();
    }

    /**
     * Wait to see messages in the log (in any order)
     * @param timeoutMs how long to wait
     * @param patterns the list of patterns to look for
     * @param rewind true to rewind messages, false to consume
     * @return a list of matched groups for each matched pattern
     * @throws IOException
     */
    public List<List<String>> waitForMessages(long timeoutMs, Pattern[] patterns, boolean rewind) throws IOException {
        @SuppressWarnings("unchecked") final List<List<String>> ret = Arrays.asList(new List[patterns.length]);
        final List<String> out = new ArrayList<>();
        final List<String> err = new ArrayList<>();
        final List<Pattern> patternList = Arrays.asList(patterns).stream().collect(Collectors.toList());
        final long timeoutTime = System.currentTimeMillis() + timeoutMs;

        while(System.currentTimeMillis() < timeoutTime && !Thread.currentThread().isInterrupted()) {
            try {
                for(Pair<StreamReader, List<String>> output: Arrays.asList(new Pair<>(stdout, out), new Pair<>(stderr, err))) {
                    String line = output.first.lines.poll(0, TimeUnit.MICROSECONDS);
                    if(line != null) {
                        output.second.add(line);
                        int idx = 0;
                        for(Iterator<Pattern> it = patternList.iterator(); it.hasNext(); ++idx) {
                            Pattern message = it.next();
                            Matcher matcher = message.matcher(line);
                            if(message.matcher(line).find()) {
                                List<String> groups = new ArrayList<>();
                                for(int groupIdx = 1; groupIdx < matcher.groupCount() + 1; ++groupIdx) {
                                    groups.add(matcher.group(groupIdx));
                                }
                                ret.set(idx, groups);
                                it.remove();
                                break;
                            }
                        }
                        if(patternList.size() == 0) {
                            if(rewind) {
                                for(int i = out.size(); --i >= 0; ) {
                                    stdout.lines.addFirst(out.get(i));
                                }
                                for(int i = err.size(); --i >= 0; ) {
                                    stderr.lines.addFirst(err.get(i));
                                }
                            }
                            return ret;
                        }
                    }

                }
            }
            catch(InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException(e);
            }
        }
        showOutput(out, err);
        throw new IOException("timed out waiting for " + patternList + " in " + fixture.getName());
    }
}
