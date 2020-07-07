package indexing;

import java.io.*;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * @author clairewalker
 *
 */
public class IndexBuilder implements IIndexBuilder {
	private Map<String, List<String>> task2map;
	private HashMap<String, Map<String, Integer>> mapOfWordsToDocCountPairs;

	/**
	 * Helper method used in ParseFeed Used to parse individual RSS feeds from list
	 * of feeds
	 * 
	 * @param rss
	 * @return List<String> html docs in rss feed
	 */
	private List<String> parseRSS(String rss) {
		List<String> htmlDocs = new LinkedList<>();
		try {
			Document doc = Jsoup.connect(rss).get();
			Elements links = doc.getElementsByTag("link");
			for (Element link : links) {
				String linkText = link.text();
				htmlDocs.add(linkText);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return htmlDocs;
	}

	/**
	 * Helper method used to parse words from individual html doc
	 * 
	 * @param url
	 * @return List<String> words in html doc
	 */
	private List<String> parseIndividualHTMLContent(String url) {
		Document doc;
		List<String> words = new ArrayList<>();
		try {
			doc = Jsoup.connect(url).get();
			String body = doc.body().text();
			String bodyNoPunctuationLowercase = body.replaceAll("\\p{Punct}", "").toLowerCase();
			words = Arrays.asList(bodyNoPunctuationLowercase.split(" "));

		} catch (IOException e) {
			e.printStackTrace();
		}

		return words;
	}

	@Override
	public Map<String, List<String>> parseFeed(List<String> feeds) {
		Map<String, List<String>> mapOfDocsAndWords = new HashMap<>();

		// for each rss feed
		for (String rss : feeds) {

			// get html docs from RSS feed
			List<String> htmlDocs = parseRSS(rss);

			// for each html doc in feed, parse it to get its list of words and make entry
			for (String doc : htmlDocs) {
				List<String> words = parseIndividualHTMLContent(doc);
				mapOfDocsAndWords.put(doc, words);
			}
		}

		this.task2map = mapOfDocsAndWords;
		return mapOfDocsAndWords;
	}

	/**
	 * Helper method for buildIndex() Interim step to build index
	 * 
	 * @return TreeMap<String, Map<String, Integer>> map. Keys are words in all the
	 *         documents, values are a map with value as the name of document the
	 *         word is in and the number of times the word appears in that document
	 */
	public HashMap<String, Map<String, Integer>> buildMapOfWordsToDocCountPairs() {
		HashMap<String, Map<String, Integer>> countsOfWordPerDoc = new HashMap<>();
		// iterate through the entries created
		for (Entry docs : task2map.entrySet()) {
			String doc = (String) docs.getKey();
			List<String> words = ((List<String>) docs.getValue());
			// for each word in the list
			for (String word : words) {
				// if the word is already in this map of counts of word per document...
				if (countsOfWordPerDoc.containsKey(word)) {
					// and if the word already has a count for this document
					if (countsOfWordPerDoc.get(word).containsKey(doc)) {
						// up the count
						countsOfWordPerDoc.get(word).put(doc, countsOfWordPerDoc.get(word).get(doc) + 1);
					}
					// else add a mapping for this doc with value 1
					else {
						countsOfWordPerDoc.get(word).put(doc, 1);
					}
				}
				// else add the word as a key with this doc, 1 as value in the value map
				else {
					Map<String, Integer> mapForThisWord = new HashMap<String, Integer>();
					mapForThisWord.put(doc, 1);
					countsOfWordPerDoc.put(word, mapForThisWord);

				}
			}
		}
		this.mapOfWordsToDocCountPairs = countsOfWordPerDoc;
		return countsOfWordPerDoc;
	}

	@Override
	public Map<String, Map<String, Double>> buildIndex(Map<String, List<String>> docs) {
		int numDocs = docs.size();

		// create new map
		Map<String, Map<String, Double>> indexMap = new TreeMap<String, Map<String, Double>>();
		// get interim map mapping words to doc/TDIFT pairs
		HashMap<String, Map<String, Integer>> mapA = buildMapOfWordsToDocCountPairs();
		// iterate through the task2map for each document
		for (Entry doc : docs.entrySet()) {
			String docname = (String) doc.getKey();
			// make new map
			Map<String, Double> wordTFIDFPairs = new TreeMap<String, Double>();
			// get the documents list of words
			List<String> wordsInThisDoc = (List<String>) doc.getValue();
			int numWordsInDoc = wordsInThisDoc.size();

			// iterate through list of words
			for (String word : wordsInThisDoc) {
				// check if word is a key in hte wordTFIDFPairs map
				if (wordTFIDFPairs.containsKey(word)) {
					// if yes, continue
					continue;
				}
				// else calculate TFIDF and add it to the treemap for this document
				else {
					Integer thisDocCount = mapA.get(word).get(docname);
					int numDocsWithThisWord = mapA.get(word).size();
					double TF = (double) thisDocCount / numWordsInDoc;
					double IDF = Math.log((double) numDocs / numDocsWithThisWord);
					double TFIDF = TF * IDF;
					wordTFIDFPairs.put(word, TFIDF);
				}
			}
			// add mapping for this document and its new map of word/TDIDF pairs
			indexMap.put(docname, wordTFIDFPairs);

		}

		return indexMap;
	}

	@Override
	public Map<?, ?> buildInvertedIndex(Map<String, Map<String, Double>> index) {
		HashMap<String, TreeSet<Entry<String, Double>>> invertedIndex = new HashMap<>();
		// iterate through all words in the interim map of words to their document/TDIFT pairs
		for (Entry mapAEntry : mapOfWordsToDocCountPairs.entrySet()) {
			String word = (String) mapAEntry.getKey();
			HashMap<String, Integer> pairsDocCount = (HashMap<String, Integer>) mapAEntry.getValue();

			// for each map entry for this word (aka for each document containing the word)
			for (Entry docPair : pairsDocCount.entrySet()) {
				//get the document name
				String docname = (String) docPair.getKey();
				//get the map that is the value for that document key in the forward index
				TreeMap<String, Double> indexPairsMap = (TreeMap<String, Double>) index.get(docname);
				//in that map, get the value associated with key of the word we are looking at
				double TDIFT = indexPairsMap.get(word);
				//if the word is already in the inverted index
				if (invertedIndex.containsKey(word)) {
					//create a new entry, and add it to that word's treeset in the inverted index
					Entry<String, Double> e = new AbstractMap.SimpleEntry<>(docname, TDIFT);
					invertedIndex.get(word).add(e);
				} else {
					//else, create a new treeset for the word
					TreeSet<Entry<String, Double>> setForThisWord = new TreeSet<Entry<String, Double>>(
							createComparator());
					//add this new entry to that treeset
					Entry<String, Double> e = new AbstractMap.SimpleEntry<>(docname, TDIFT);
					//and add the <word, treeset> pair to the inverted index
					setForThisWord.add(e);
					invertedIndex.put(word, setForThisWord);
				}
			}

		}
		return invertedIndex;
	}

	/**
	 * creates comparator used in building the inverted index Each word has a map of
	 * documents and corresponding TDIFT scores This orders those pairs from highest
	 * to lowest score. Ties are broken based on alpha order
	 * 
	 * @return Comparator<Entry<String, Double>>
	 */
	private Comparator<Entry<String, Double>> createComparator() {
		Comparator<Entry<String, Double>> comp = new Comparator<Entry<String, Double>>() {
			@Override
			public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {
				if (o1.getValue().equals(o2.getValue())) {
					return o1.getKey().compareTo(o2.getKey());
				} else
					return o2.getValue().compareTo(o1.getValue());
			}
		};

		return comp;
	}

	@Override
	public Collection<Entry<String, List<String>>> buildHomePage(Map<?, ?> invertedIndex) {
		// create new treeset
		TreeSet<Entry<String, List<String>>> homepage = new TreeSet<Entry<String, List<String>>>(
				createComparatorHomePage());
		// iterate through the inverted index
		for (Entry<?, ?> e : invertedIndex.entrySet()) {
			// if the key is a stop word, skip it
			String word = (String) e.getKey();
			if (STOPWORDS.contains(word)) {
				continue;
			}
			// else, create a list from the value map
			else {
				List<String> docsList = new LinkedList<String>();
				// get the key's value
				Set<Entry> wordSet = (Set) e.getValue();
				// iterate through all entries in this set, storing its key into a new list
				for (Entry<?, ?> docPair : wordSet) {
					String docname = (String) docPair.getKey();
					docsList.add(docname);
				}
				Entry<String, List<String>> homepageEntry = new AbstractMap.SimpleEntry<>(word, docsList);
				homepage.add(homepageEntry);
			}

		}
		return homepage;
	}

	/**
	 * Creates comparator used in buildHomePage Orders words by the number of
	 * documents they are in, or in reverse lexxicographic order if they are in the
	 * same number of documents
	 * 
	 * @return Comparator<Entry<String, List<String>>>
	 */
	private Comparator<Entry<String, List<String>>> createComparatorHomePage() {
		Comparator<Entry<String, List<String>>> comp = new Comparator<Entry<String, List<String>>>() {
			@Override
			public int compare(Entry<String, List<String>> o1, Entry<String, List<String>> o2) {
				if (o1.getValue().size() == (o2.getValue().size())) {
					return o2.getKey().compareTo(o1.getKey());
				} else
					return o2.getValue().size() - o1.getValue().size();
			}
		};

		return comp;
	}

	@Override
	public Collection<?> createAutocompleteFile(Collection<Entry<String, List<String>>> homepage) {
		TreeSet<String> wordsWritten = new TreeSet<String>();
		int numWords = homepage.size();
		String numWordsAsAString = String.valueOf(numWords);
		// create new file
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter("autocomplete.txt"));
			// write the number of words to the file
			bw.write(numWordsAsAString);
			bw.newLine();
			// go through the homepage collection
			for (Entry<?, ?> e : homepage) {
				String word = (String) e.getKey();
				// for each word, write a line to the file
				bw.write("  1 " + word);
				bw.newLine();
				// and save it to the collection
				wordsWritten.add(word);
			}

			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return wordsWritten;
	}

	@Override
	public List<String> searchArticles(String queryTerm, Map<?, ?> invertedIndex) {
		List<String> docsList = new LinkedList<String>();
		// if the key is not in the inverted index, return an empty list
		if (!invertedIndex.containsKey(queryTerm)) {
			return docsList;
		}

		// find the word as a key in the inverted index
		Set<Entry> wordSet = (Set) invertedIndex.get(queryTerm);
		// iterate through all entries in this map, storing its key into a new list
		for (Entry<?, ?> docPair : wordSet) {
			String docname = (String) docPair.getKey();
			docsList.add(docname);
		}
		return docsList;
	}

}
