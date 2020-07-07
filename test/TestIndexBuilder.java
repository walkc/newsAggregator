package test;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;

import org.junit.Test;

import indexing.IndexBuilder;

/**
 * @author ericfouh
 */
public class TestIndexBuilder {

	/**
	 * Test that parseFeed()returns map of correct size. For test set, should be 5
	 */
	@Test
	public void testParseFeedSize() {
		IndexBuilder test = new IndexBuilder();
		List<String> rssFeeds = new ArrayList<>();
		rssFeeds.add("http://localhost:8090/sample_rss_feed.xml");
		Map<String, List<String>> m = test.parseFeed(rssFeeds);
		assertEquals(m.size(), 5);
	}

	/**
	 * Test that parseFeed()map contains names of documents as keys. Checks that
	 * "http://localhost:8090/page1.html", "http://localhost:8090/page3.html",
	 * "http://localhost:8090/page3.html", "http://localhost:8090/page4.html",
	 * "http://localhost:8090/page5.html" are all keys in the map
	 */
	@Test
	public void testParseFeedKeys() {
		IndexBuilder test = new IndexBuilder();
		List<String> rssFeeds = new ArrayList<>();
		rssFeeds.add("http://localhost:8090/sample_rss_feed.xml");
		Map<String, List<String>> m = test.parseFeed(rssFeeds);
		assertNotNull(m.get("http://localhost:8090/page1.html"));
		assertNotNull(m.get("http://localhost:8090/page2.html"));
		assertNotNull(m.get("http://localhost:8090/page3.html"));
		assertNotNull(m.get("http://localhost:8090/page4.html"));
		assertNotNull(m.get("http://localhost:8090/page5.html"));
	}

	/**
	 * Test that parseFeed()map contains correct number of files for each document
	 * in the test set
	 */
	@Test
	public void testParseFeedCorrectWordsForDocKey() {
		IndexBuilder test = new IndexBuilder();
		List<String> rssFeeds = new ArrayList<>();
		rssFeeds.add("http://localhost:8090/sample_rss_feed.xml");
		Map<String, List<String>> m = test.parseFeed(rssFeeds);
		assertTrue(m.get("http://localhost:8090/page1.html").size() == 10);
		assertTrue(m.get("http://localhost:8090/page2.html").size() == 55);
		assertTrue(m.get("http://localhost:8090/page3.html").size() == 33);
		assertTrue(m.get("http://localhost:8090/page4.html").size() == 22);
		assertTrue(m.get("http://localhost:8090/page5.html").size() == 18);
	}

	/**
	 * Test that buildIndex()returns map of correct size. For test set, should be 5
	 */
	@Test
	public void testBuildIndexRightSize() {
		IndexBuilder test = new IndexBuilder();
		List<String> rssFeeds = new ArrayList<>();
		rssFeeds.add("http://localhost:8090/sample_rss_feed.xml");
		Map<String, List<String>> m = test.parseFeed(rssFeeds);
		Map<String, Map<String, Double>> index = test.buildIndex(m);
		assertEquals(index.size(), 5);
	}

	/**
	 * Tests that investedIndex is of expected size, equal to number of unique
	 * words. in this test set, should be 92
	 */
	@Test
	public void testBuildInvertedIndexRightSize() {
		IndexBuilder test = new IndexBuilder();
		List<String> rssFeeds = new ArrayList<>();
		rssFeeds.add("http://localhost:8090/sample_rss_feed.xml");
		Map<String, List<String>> m = test.parseFeed(rssFeeds);
		Map<String, Map<String, Double>> index = test.buildIndex(m);
		Map<?, ?> invertedIndex = test.buildInvertedIndex(index);
		assertEquals(invertedIndex.size(), 92);
	}

	/**
	 * Tests that investedIndex is of correct type, hashmap
	 */
	@Test
	public void testBuildInvertedIndexRightType() {
		IndexBuilder test = new IndexBuilder();
		List<String> rssFeeds = new ArrayList<>();
		rssFeeds.add("http://localhost:8090/sample_rss_feed.xml");
		Map<String, List<String>> m = test.parseFeed(rssFeeds);
		Map<String, Map<String, Double>> index = test.buildIndex(m);
		Map<?, ?> invertedIndex = test.buildInvertedIndex(index);
		assertTrue(invertedIndex instanceof HashMap);
	}

	/**
	 * Tests that investedIndex associates correct files to a term in correct order.
	 * Order by TDITF scores. For term data, order should be: page1, page2, page3
	 */
	@Test
	public void testBuildInvertedIndexRightFilesForTerm() {
		IndexBuilder test = new IndexBuilder();
		List<String> rssFeeds = new ArrayList<>();
		rssFeeds.add("http://localhost:8090/sample_rss_feed.xml");
		Map<String, List<String>> m = test.parseFeed(rssFeeds);
		Map<String, Map<String, Double>> index = test.buildIndex(m);
		Map<?, ?> invertedIndex = test.buildInvertedIndex(index);
		TreeSet<Entry<String, Double>> dataSet = (TreeSet<Entry<String, Double>>) invertedIndex.get("data");
		ArrayList listOfDocs = new ArrayList();
		for (Entry e : dataSet) {
			String doc = (String) e.getKey();
			listOfDocs.add(doc);
		}
		assertEquals(listOfDocs.get(0), "http://localhost:8090/page1.html");
		assertEquals(listOfDocs.get(1), "http://localhost:8090/page2.html");
		assertEquals(listOfDocs.get(2), "http://localhost:8090/page3.html");
	}

	/**
	 * Tests the homepage contains correct numnber of terms, equal to unique words
	 * in all documents minus stopwords. For this test set, should be 57
	 */
	@Test
	public void testBuildHomepageRightSize() {
		IndexBuilder test = new IndexBuilder();
		List<String> rssFeeds = new ArrayList<>();
		rssFeeds.add("http://localhost:8090/sample_rss_feed.xml");
		Map<String, List<String>> m = test.parseFeed(rssFeeds);
		Map<String, Map<String, Double>> index = test.buildIndex(m);
		Map<?, ?> invertedIndex = test.buildInvertedIndex(index);
		Collection<Entry<String, List<String>>> homepage = test.buildHomePage(invertedIndex);
		assertEquals(homepage.size(), 57);
	}

	/**
	 * Tests the homepage is of correct type, TreeSet
	 */
	@Test
	public void testBuildHomepageRightType() {
		IndexBuilder test = new IndexBuilder();
		List<String> rssFeeds = new ArrayList<>();
		rssFeeds.add("http://localhost:8090/sample_rss_feed.xml");
		Map<String, List<String>> m = test.parseFeed(rssFeeds);
		Map<String, Map<String, Double>> index = test.buildIndex(m);
		Map<?, ?> invertedIndex = test.buildInvertedIndex(index);
		Collection<Entry<String, List<String>>> homepage = test.buildHomePage(invertedIndex);
		assertTrue(homepage instanceof TreeSet);
	}

	/**
	 * Tests that searchArticles() returns correct num of articles for search term.
	 * Term data should have 3 articles returned
	 */
	@Test
	public void testSearchArticlesCorrectNumberArticles() {
		IndexBuilder test = new IndexBuilder();
		List<String> rssFeeds = new ArrayList<>();
		rssFeeds.add("http://localhost:8090/sample_rss_feed.xml");
		Map<String, List<String>> m = test.parseFeed(rssFeeds);
		Map<String, Map<String, Double>> index = test.buildIndex(m);
		Map<?, ?> invertedIndex = test.buildInvertedIndex(index);
		List<String> listOfArticles = test.searchArticles("data", invertedIndex);
		assertEquals(listOfArticles.size(), 3);
	}

	/**
	 * Tests that searchArticles() returns correct articles for search term. Term
	 * data should return page1.html, page2.html, and page3.html
	 */
	@Test
	public void testSearchArticlesCorrectArticles() {
		IndexBuilder test = new IndexBuilder();
		List<String> rssFeeds = new ArrayList<>();
		rssFeeds.add("http://localhost:8090/sample_rss_feed.xml");
		Map<String, List<String>> m = test.parseFeed(rssFeeds);
		Map<String, Map<String, Double>> index = test.buildIndex(m);
		Map<?, ?> invertedIndex = test.buildInvertedIndex(index);
		List<String> listOfArticles = test.searchArticles("data", invertedIndex);
		assertTrue(listOfArticles.contains("http://localhost:8090/page1.html")
				&& listOfArticles.contains("http://localhost:8090/page2.html")
				&& listOfArticles.contains("http://localhost:8090/page3.html"));
	}

	/**
	 * Tests the createAutocompleteFile() returns correct number of terms. For test
	 * set, should return 57 terms
	 */
	@Test
	public void testWriteAutoFileCorrectSize() {
		IndexBuilder test = new IndexBuilder();
		List<String> rssFeeds = new ArrayList<>();
		rssFeeds.add("http://localhost:8090/sample_rss_feed.xml");
		Map<String, List<String>> m = test.parseFeed(rssFeeds);
		Map<String, Map<String, Double>> index = test.buildIndex(m);
		Map<?, ?> invertedIndex = test.buildInvertedIndex(index);
		Collection<Entry<String, List<String>>> homepage = test.buildHomePage(invertedIndex);
		Collection<?> wordsWritten = test.createAutocompleteFile(homepage);
		assertEquals(wordsWritten.size(), 57);
	}

	/**
	 * Tests the createAutocompleteFile() is of correct type TreeSet
	 */
	@Test
	public void testWriteAutoFileCorrectType() {
		IndexBuilder test = new IndexBuilder();
		List<String> rssFeeds = new ArrayList<>();
		rssFeeds.add("http://localhost:8090/sample_rss_feed.xml");
		Map<String, List<String>> m = test.parseFeed(rssFeeds);
		Map<String, Map<String, Double>> index = test.buildIndex(m);
		Map<?, ?> invertedIndex = test.buildInvertedIndex(index);
		Collection<Entry<String, List<String>>> homepage = test.buildHomePage(invertedIndex);
		Collection<?> wordsWritten = test.createAutocompleteFile(homepage);
		assertTrue(wordsWritten instanceof TreeSet);
	}

}
