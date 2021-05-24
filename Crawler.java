package com.company;
import java.net.*;
import java.util.*;
import java.util.regex.*;

class Crawler {
    class URLDepthPair {
        private String url;
        private int depth;

        public URLDepthPair(String url, int depth) {
            this.url = url;
            this.depth = depth;
        }

        public String getURL() {
            return url;
        }

        public int getDepth() {
            return depth;
        }

        public String toString() {
            return "Глубина: " + depth + "\tURL: " + url;
        }
    }

    private HashMap<String, URLDepthPair> links = new HashMap<>();
    private LinkedList<URLDepthPair> pool = new LinkedList<>();
    private int depth = 0;

    public Crawler(String url, int depth) {
        this.depth = depth;
        pool.add(new URLDepthPair(url, 0));
    }

    public void find() {
        int c = Thread.activeCount();
        while (pool.size() > 0 || Thread.activeCount() > c) {
            if (pool.size() > 0) {
                URLDepthPair link = pool.pop();
                CrawlerThread task = new CrawlerThread(link);
                Thread tr = new Thread(task);
                tr.start();
            }
        }
        System.out.println("\nНайдено: " + links.size() + "\n");
        for (URLDepthPair link : links.values())
            System.out.println(link);
    }

    private class CrawlerThread implements Runnable {
        private URLDepthPair link;

        public CrawlerThread(URLDepthPair link_) {
            link = link_;
        }

        @Override
        public void run() {
            if (links.containsKey(link.getURL())) return;
            links.put(link.getURL(), link);
            if (link.getDepth() >= depth) return;
            try {
                URL url = new URL(link.getURL());
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                Scanner s = new Scanner(con.getInputStream());
                Pattern LINK_REGEX = Pattern.compile("<a\\s+(?:[^>]*?\\s+)?href=([\"'])(.*?)\\1");
                while (s.findWithinHorizon(LINK_REGEX, 0) != null) {
                    String newURL = s.match().group(2);
                    if (newURL.startsWith("/"))
                        newURL = link.getURL() + newURL;
                    else if (!newURL.startsWith("http"))
                        continue;
                    URLDepthPair newLink = new URLDepthPair(newURL, link.getDepth() + 1);
                    pool.add(newLink);
                }
            } catch (Exception e) {
            }
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("usage: java Crawler <URL> <depth>");
            System.exit(1);
        }
        String url = args[0];
        int depth = 0;
        try {
            depth = Integer.parseInt(args[1]);
        } catch (Exception e) {
            System.out.println("usage: java Crawler <URL> <depth>");
            System.exit(1);
        }
        Crawler crawler = new Crawler(url, depth);
        crawler.find();
    }
}
