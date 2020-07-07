package autocomplete;
/**
 * @author clairewalker
 *
 */
public class Term implements ITerm {
	private String query;
	private long weight;

	/**
	 * @param query
	 * @param weight
	 * @throws IllegalArgumentException
	 */
	public Term(String query, long weight) throws IllegalArgumentException {
		if (query == null || weight < 0) {
			throw new IllegalArgumentException();
		}

		this.query = query;
		this.weight = weight;
	}

	@Override
	public int compareTo(ITerm that) {
		return this.query.compareTo(((Term) that).getTerm());
	}

	/**
	 * @return weight
	 */
	public long getWeight() {
		return weight;
	}

	/**
	 * @param weight set this.weight to weight
	 */
	public void setWeight(int weight) {
		this.weight = weight;
	}

	/**
	 * @return query
	 */
	public String getTerm() {
		return this.query;
	}

	/**
	 * @param q set query to q
	 */
	public void setTerm(String q) {
		this.query = q;
	}

	@Override
	public String toString() {
		return this.weight + "\t" + this.query;
	}

}
