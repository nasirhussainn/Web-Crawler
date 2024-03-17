import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WebCrawler {
    private final int maxPages;
    private final String seedUrl;
    private final String outputDir;
    private final Set<String> visitedUrls;
    private int pagesCrawled;

    public WebCrawler(String seedUrl, int maxPages, String outputDir) {
        this.seedUrl = seedUrl;
        this.maxPages = maxPages;
        this.outputDir = outputDir;
        this.visitedUrls = new HashSet<>();
        this.pagesCrawled = 0;
    }

    public void crawl() throws IOException {
        Queue<String> queue = new LinkedList<>();
        queue.add(seedUrl);

        while (!queue.isEmpty() && pagesCrawled < maxPages) {
            String url = queue.poll();
            if (!visitedUrls.contains(url)) {
                try {
                    Document doc = Jsoup.connect(url).get();
                    visitedUrls.add(url);
                    pagesCrawled++;

                    String pageTitle = doc.title().trim();
                    String pageBody = doc.body().text().trim();
                    String pageUri = url;

                    StringBuilder headingsText = new StringBuilder();
                    Elements headings = doc.select("h1, h2, h3, h4, h5, h6");
                    for (Element heading : headings) {
                        headingsText.append(heading.text()).append("\n");
                    }

                    FileWriter writer = new FileWriter(outputDir + "/" + pagesCrawled + "_" + cleanFileName(pageTitle) + ".txt");
                    writer.write("URL: " + pageUri + "\n");
                    writer.write("Title: " + pageTitle + "\n");
                    writer.write("Body: \n" + pageBody + "\n");
                    writer.write("Headings: \n" + headingsText.toString());
                    writer.close();

                    System.out.println("Crawled ("+ pagesCrawled +") : "+ url);

                    Elements links = doc.select("a[href]");
                    for (Element link : links) {
                        String nextUrl = link.absUrl("href");
                        if (!nextUrl.isEmpty()) {
                            queue.add(nextUrl);
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Error crawling " + url + ": " + e.getMessage());
                    continue;
                }
            }
        }
    }

    private String cleanFileName(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
    }

    public static void main(String[] args) {
        String seedUrl = "https://developers.google.com";
        int maxPages = 1000;
        String outputDir = "crawled_pages";

        WebCrawler crawler = new WebCrawler(seedUrl, maxPages, outputDir);
        try {
            crawler.crawl();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
