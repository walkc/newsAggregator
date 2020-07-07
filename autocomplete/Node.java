package autocomplete;
/**
 * ==== Attributes ==== - words: number of words - term: the ITerm object -
 * prefixes: number of prefixes - references: Array of references to
 * next/children Nodes
 * 
 * ==== Constructor ==== Node(String word, long weight)
 * 
 * @author Your_Name
 */
public class Node {
	private int words;
	private ITerm term;
	private int prefixes;
	private Node[] references;

	/**
	 * @param word
	 * @param weight
	 * @throws IllegalArgumentException
	 */
	public Node(String word, long weight) throws IllegalArgumentException {
		if (word == null || weight < 0) {
			throw new IllegalArgumentException();
		}
		this.words = 0;
		if (weight < 1) {
			term = null;
		} else {
			term = new Term(word, weight);
		}
		references = new Node[26];
	}

	/**
	 * Constructor with no params
	 */
	public Node() {
		this.words = 0;
//		this.term = null;
		this.prefixes = 0;
		this.references = new Node[26];
	}

	
	/**
	 * @return term weight
	 */
	public long getWeight() {
		return ((Term) this.term).getWeight();
	}

	/**
	 * @param w
	 */
	public void setWeight(int w) {
		((Term) this.term).setWeight(w);
	}

	/**
	 * @return this.term
	 */
	public Term getTerm() {
		return (Term) this.term;
	}

	/**
	 * @param word
	 * @param weight
	 */
	public void setTerm(String word, long weight) {
		this.term = new Term(word, weight);
	}

	/**
	 * @param t sets this.term to t
	 */
	public void setTerm(Term t) {
		this.term = t;
	}

	/**
	 * @return words
	 */
	public int getWords() {
		return this.words;
	}

	/**
	 * @param w
	 * @return w sets words=w
	 */
	public int setWords(int w) {
		this.words = w;
		return w;
	}

	/**
	 * @return prefixes
	 */
	public int getPrefixes() {
		return prefixes;
	}

	/**
	 * @param p sets this.prefixes=p
	 */
	public void setPrefixes(int p) {
		if (p < 0) {
			return;
		}
		prefixes = p;
	}

	/**
	 * @param i
	 * @return references[i]
	 */
	public Node getReference(int i) {
		if (i < 0 || i > 25) {
			return null;
		}
		return references[i];
	}

	/**
	 * @param n
	 * @param i sets reference for index i to Node n
	 */
	public void setReference(Node n, int i) {
		if (i < 0 || i > 25) {
			return;
		}
		references[i] = n;
	}

	/**
	 * @return this.references
	 */
	public Node[] getReferences() {
		return this.references;
	}

	/**
	 * @param refs
	 */
	public void setReferences(Node[] refs) {
		this.references = refs;
	}

	/**
	 * @return true is Node is leaf
	 */
	public boolean isLeaf() {
		for (int i = 0; i < 26; i++) {
			if (this.references[i] != null) {
				return false;
			}
		}
		return true;
	}

}
