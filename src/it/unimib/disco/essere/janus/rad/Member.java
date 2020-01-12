package it.unimib.disco.essere.janus.rad;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import it.unimib.disco.essere.janus.preprocessing.Instance;
import it.unimib.disco.essere.janus.preprocessing.InstancesHandler;
import it.unimib.disco.essere.janus.rad.exception.FitnessValueNotComputedException;

public class Member{
	
	private final int length;
	
	/**
	 * The list of bits, the index of bit match with 
	 * the ID of the related method
	 */
	private boolean[] bits;

	private List<Instance> selectedInstances;
	
	private Map<String, Integer> clonedStatement;

	private Double fitnessValue = null;
	
	/**
	 * @param length The number of bits.
	 */
	public Member(int length, InstancesHandler code_handler) {
		if (length <= 0)
			throw new IllegalArgumentException("Length must be positive.");
		this.length = length;
		bits = new boolean[length];
	}


	/**
	 * @param length The number of bits.
	 * @param rng A source of randomness.
	 * @throws Exception 
	 */
	public Member(int length, Random rng, InstancesHandler code_handler) throws Exception{
		this(length, code_handler);
		for (int i = 0; i < bits.length; i++)
			bits[i] = rng.nextBoolean();
		this.updateSelectedInstance(code_handler);
	}

	/**
	 * @param length 		The number of bits.
	 * @param indexOfOnes	The indexes that have to 
	 * 						be set to 1 in the binary string
	 * @throws Exception 
	 */
	public Member(int length, Set<Integer> indexOfOnes, InstancesHandler code_handler) {
		this(length, code_handler);
		for(int index: indexOfOnes)
			bits[index] = true;
		this.updateSelectedInstance(code_handler);
	}
	
//	public Member(Solution s, InstancesHandler code_handler) {
//		String bits = "";
//		for(int i=0; i < s.getNumberOfVariables(); i++) {
//			bits += s.getVariable(i).toString();
//		}
//		this(bits, code_handler);
//	}

	/**
	 * Initializes the bit string from a character string of 1s and 0s
	 * @param value A character string of ones and zeros.
	 * @throws Exception 
	 */    
	public Member(String value, InstancesHandler code_handler) {
		this(value.length(), code_handler);
		for (int i = 0; i < value.length(); i++){
			if (value.charAt(i) == '1')
				bits[i] = true;
			else if (value.charAt(i) != '0')
				throw new IllegalArgumentException("Illegal character at position " + i);
		}
		this.updateSelectedInstance(code_handler);
	}

	/**
	 * Initializes the bit string from a character string of 1s and 0s
	 * @param value A character string of ones and zeros.
	 * @throws Exception 
	 */    
	public Member(boolean[] bits, InstancesHandler code_handler) {
		this.length = bits.length;
		this.bits = bits;
		this.updateSelectedInstance(code_handler); 
	}

	public List<Instance> getSelectedInstances() {
		return selectedInstances;
	}

	public boolean isEvaluated() {
		return fitnessValue != null;
	}

	public double getFitnessValue(){
		assertEvaluated();
		return fitnessValue;
	}


	public void setFitnessValue(double fitnessValue) {
		this.fitnessValue = fitnessValue;
	}
	
	public int countOnes() {
		int count = 0;
		for(boolean b: bits)
			count = b? count+1 : count;
		return count;
	}


	/**
	 * @return The length of this bit string.
	 */
	public int getLength(){
		return length;
	}

	/**
	 * Returns the bit at the specified index.
	 * @param index The index of the bit to look-up (0 is the least-significant bit).
	 * @return A boolean indicating whether the bit is set or not.
	 * @throws IndexOutOfBoundsException If the specified index is not a bit
	 * position in this bit string.
	 */
	public boolean getBit(int index){
		assertValidIndex(index);
		return bits[index];
	}

	/**
	 * Sets the bit at the specified index.
	 * @param index The index of the bit to set (0 is the least-significant bit).
	 * @param set A boolean indicating whether the bit should be set or not.
	 * @throws IndexOutOfBoundsException If the specified index is not a bit
	 * position in this bit string.
	 */
	public void setBit(int index, boolean set) {
		assertValidIndex(index);
		bits[index] = set;
	}

	/**
	 * Inverts the value of the bit at the specified index.
	 * @param index The bit to flip (0 is the least-significant bit).
	 * @throws IndexOutOfBoundsException If the specified index is not a bit
	 * position in this bit string.     
	 */
	public void flipBit(int index){
		assertValidIndex(index);
		bits[index] = !(bits[index]);
	}

	public void updateSelectedInstance(InstancesHandler ins){
		List<Instance> methods = new ArrayList<Instance>();
		try {
			for(int i = 0; i < bits.length; i++) {
				if(bits[i]) {
					//int index = InstancesHandbook.getInstance().getInstances().get(i);
					//methods.add(InstancesHandbook.getInstance().getInstance(index));
					methods.add(ins.getMethod(i));
				}
			}
		}catch (NullPointerException e) {
			System.out.println("[WARNING] Member without population instantiated");
		}
		this.selectedInstances = methods;
	}



	/**
	 * Creates a textual representation of this bit string 
	 * @return This bit string rendered as a String of 1s and 0s.
	 */
	@Override
	public String toString(){
		String result = "";
		for (boolean bit: bits){
			if(bit)
				result = result + 1;
			else
				result = result + 0;
		}
		return result;
	}

	public boolean[] getBitString() {
		return bits;
	}

	private void assertEvaluated(){
		if(fitnessValue == null)
			throw new FitnessValueNotComputedException();
	}

	/**
	 * Helper method to check whether a bit index is valid or not.
	 * @param index The index to check.
	 * @throws IndexOutOfBoundsException If the index is not valid.
	 */
	private void assertValidIndex(int index){
		if (index >= length || index < 0){
			throw new IndexOutOfBoundsException("Invalid index: " + index + " (length: " + length + ")");
		}
	}


	public Map<String, Integer> getClonedStatement() {
		return clonedStatement;
	}


	public void setClonedStatement(Map<String, Integer> clonedStatement) {
		this.clonedStatement = clonedStatement;
	}

}
