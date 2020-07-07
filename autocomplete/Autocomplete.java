package autocomplete;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author clairewalker
 *
 */
public class Autocomplete implements IAutocomplete {
	private Node root;
	private Node current;
	private int numberSuggestions;

	/**
	 * Creates new Autocomplete object with root node that has 0 prefixes and query
	 * "" Sets current node to root node
	 */
	public Autocomplete() {
		this.root = new Node("", 0);
		this.current = root;
	}

	/**
	 * @return Node root
	 */
	public Node getRoot() {
		return this.root;
	}

	/**
	 * @param r sets root to Node r
	 */
	public void setRoot(Node r) {
		this.root = r;
	}

	/**
	 * helper method for addWord param word, fullWord, weight word is the string
	 * that is being cut by one letter for each iteration fullWord remains untouched
	 * to be added to the Term once the Node is created this function does the work
	 * of adding the word to the Trie
	 * 
	 */
	private void addWordHelper(String word, String fullWord, long weight) {
		// get the first letter of the word
		char firstLetter = word.charAt(0);
		int indexOfLetter = firstLetter - 97;
		// if index of letter is not between 0 and 25 after this calc, then it's not a
		// valid lowercase letter
		if (indexOfLetter < 0 || indexOfLetter > 25) {
			return;
		}

		// if the word length is 1, then we want to add the word to the Trie
		if (word.length() == 1) {
			// if this letter's reference index is currently null, then we create a new node
			// and add it to the Trie
			if (current.getReference(indexOfLetter) == null) {
				// creates a new node whose term is null
				Node newNode = new Node(fullWord, weight);
				newNode.setPrefixes(1);
				newNode.setWords(1);
				current.setReference(newNode, indexOfLetter);
				current = root;
				root.setPrefixes(root.getPrefixes() + 1);
				return;
			}
			// else, we want to update the node at this location in the Trie
			else {
				// move current
				current = current.getReference(indexOfLetter);
				// increment prefixes, words, and set the term for this node
				current.setPrefixes(current.getPrefixes() + 1);
				current.setWords(1);
				current.setTerm(fullWord, weight);
				// reset the current pointer back to the root and increment root's prefixes
				current = root;
				root.setPrefixes(root.getPrefixes() + 1);
				return;
			}
		}

		// if word length is not 1, then want to move to the correct reference[]
		// location for this letter
		// if the reference is currently null, then we want to create a new node
		if (current.getReference(indexOfLetter) == null) {
			// creates a new node whose term is null
			Node newNode = new Node(word, 0);
			newNode.setPrefixes(1);
			current.setReference(newNode, indexOfLetter);
			current = newNode;
		} else {
			// move current
			current = current.getReference(indexOfLetter);
			// increment the number of prefixes for that letter's reference
			current.setPrefixes(current.getPrefixes() + 1);
		}
		// recursive call on the next letter of the word
		addWordHelper(word.substring(1), fullWord, weight);

	}

	@Override
	public void addWord(String word, long weight) {
		// if the word length is less than 1, return
		if (word.length() < 1) {
			return;
		}

		// make sure word is lowercase
		String lowerCaseWord = word.toLowerCase();

		// call addWordHelper with this lowercase word
		// we pass two copies because one copy will be chopped in the recursive process
		addWordHelper(lowerCaseWord, lowerCaseWord, weight);
		current = root;
	}

	@Override
	public Node buildTrie(String filename, int k) {
		long weight;
		numberSuggestions = k;
		String lineRead = "";
		int count = 0;

		try {
			// try to read the file
			BufferedReader br = new BufferedReader(new FileReader(filename));
			br.readLine();
			lineRead = br.readLine();
			// iterates through the file reading line by line
			while (lineRead != null) {
				// trims white spaces and splits the word and its weight into an array
				String trimmmedString = lineRead.trim();
				String[] splitLineArray = trimmmedString.split("\\s+");
				try {
					// converts the weight to a long
					weight = Long.parseLong(splitLineArray[0]);

				}
				// if there is a NumberFormatException, skip this line
				catch (NumberFormatException e) {
					lineRead = br.readLine();
					continue;
				}
				// get the word and the weight from the splitLienArray

				if (splitLineArray.length < 2) {
					lineRead = br.readLine();
					continue;
				}

				String word = splitLineArray[1];

				if (weight < 0) {
					lineRead = br.readLine();
					continue;
				}
				// pass the word and weight as parameters to addWord
				addWord(word, weight);
				// keep reading file
				lineRead = br.readLine();
			}

			br.close();
		}
		// catch exceptions in opening or reading file
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}

		// reset the current to the root and return the root
		current = root;
		return root;
	}

	@Override
	public int numberSuggestions() {
		return numberSuggestions;
	}

	@Override
	public Node getSubTrie(String prefix) {
		Node nodeToReturn = root;

		// if prefix is empty string, return the root
		if (prefix.length() < 1) {
			if (prefix.equals("")) {
				return root;
			} else {
				return null;
			}
		}

		// make sure word is lowercase
		String lowerCaseWord = prefix.toLowerCase();

		// get the first letter
		char firstLetter = lowerCaseWord.charAt(0);
		int indexOfLetter = firstLetter - 97;

		// if prefix is one letter long, return the reference for that letter or null if
		// it doesn't exist
		if (prefix.length() == 1) {
			if (current.getReference(indexOfLetter) == null) {
				return null;
			} else {
				nodeToReturn = current.getReference(indexOfLetter);
				return nodeToReturn;
			}
		}

		// else, move to the reference for that letter, returning null if it doesn't
		// exist
		if (current.getReference(indexOfLetter) == null) {
			return null;
		} else {
			// move current
			current = current.getReference(indexOfLetter);
		}
		// do recursive call on the rest of the string
		nodeToReturn = getSubTrie(prefix.substring(1));
		current = root;
		return nodeToReturn;
	}

	@Override
	public int countPrefixes(String prefix) {
		Node subTrieRoot = getSubTrie(prefix);
		if (subTrieRoot == null) {
			return 0;
		}
		return subTrieRoot.getPrefixes();
	}

	@Override
	public List<ITerm> getSuggestions(String prefix) {
		List<ITerm> listOfQueries = new ArrayList<ITerm>();
		Node subTrieRoot = getSubTrie(prefix);
		if (subTrieRoot == null) {
			return listOfQueries;
		}
		getSuggestionsHelper(subTrieRoot, listOfQueries);
		return listOfQueries;
	}

	/**
	 * helper method for getSuggestions receives the root of the subtrie and an
	 * empty list adds all words in the subtrie to the list
	 * 
	 */
	private void getSuggestionsHelper(Node subTrieRoot, List<ITerm> list) {
		if (subTrieRoot.getWords() == 1) {
			list.add(subTrieRoot.getTerm());
		}
//		if (subTrieRoot.getTerm()!=null) {
//			list.add(subTrieRoot.getTerm());
//		}
		if (subTrieRoot.isLeaf()) {
			return;
		} else {
			for (int i = 0; i < 26; i++) {
				if (subTrieRoot.getReference(i) != null) {
					getSuggestionsHelper(subTrieRoot.getReference(i), list);
				}
			}
		}
	}

}
