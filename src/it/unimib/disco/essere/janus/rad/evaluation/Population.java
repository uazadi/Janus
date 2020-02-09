package it.unimib.disco.essere.janus.rad.evaluation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import it.unimib.disco.essere.janus.preprocessing.InstancesHandler;
import it.unimib.disco.essere.janus.preprocessing.MethodHandler;
import it.unimib.disco.essere.janus.rad.evaluation.Member;
import it.unimib.disco.essere.janus.rad.exception.FitnessValueNotComputedException;
import it.unimib.disco.essere.janus.rad.exception.NotInRangeNumberOfOnesException;
import it.unimib.disco.essere.janus.rad.exception.PopulationNotCreatedException;


/**
 * @author umberto
 *
 */
public class Population {
	
	private static int DEFAULT_MIN_LINES_FOR_METHODS = 5; 
	
	private static int DEFAULT_MIN_NUMBER_OF_METHOD_SELECT = 2;
	
	private static int DEFAULT_MAX_NUMBER_OF_METHOD_SELECT = 20;

	/** The current population expressed as a list of bit strings*/
	private List<Member> population;
	
	/** The length of the string of bits that represent a member of the population */
	private int memberSize;
	
	/** the minimum number of methods that as to be selected in order to consider a member valid */
	private int minNumOfOnes;
	
	/** the maximum number of methods that as to be selected in order to consider a member valid */
	private int maxNumOfOnes; 
	
	
	private InstancesHandler code_handler;
	
	public Population (Population p){
		this(p.minNumOfOnes, p.maxNumOfOnes, p.code_handler);
		this.memberSize = p.memberSize;
		/* This method allows to not modify the 
		 * filterMethodsByLines, this is necessary in order
		 * to let the new population work with the same set 
		 * of methods */
	}
	
	/**
	 * 
	 * @param minNumOfOnes	the minimum number of methods methods that as to be
	 * 						selected in order to consider a member valid
	 * @param maxNumOfOnes	the maximum number of methods methods that as to be
	 * 						selected in order to consider a member valid
	 * @throws Exception 
	 */
	public Population (int minNumOfOnes, int maxNumOfOnes, InstancesHandler code_handler){
		if(maxNumOfOnes < minNumOfOnes)
			throw new RuntimeException("The minimum number of ones requested "
					+ "is greater then the maximum number of ones requested");
		
		memberSize = code_handler.getNumOfInstance();
		this.minNumOfOnes = minNumOfOnes;
		this.maxNumOfOnes = maxNumOfOnes;
		
	}

	public Population(InstancesHandler code_handler) {
		this(DEFAULT_MIN_NUMBER_OF_METHOD_SELECT, 
			 DEFAULT_MAX_NUMBER_OF_METHOD_SELECT,
			 code_handler);
	}


	public void generateInitialPopulation(
			int populationSize, 
			InstancesHandler code_handler) throws Exception { 
		population = new ArrayList<Member>();
		for(int i=0; i < populationSize; i++)
			population.add(new Member(memberSize, new Random(), code_handler));
	}
	
	public void generateInitialPopulation(
			int populationSize, 
			int numOfOnes, 
			InstancesHandler code_handler) throws Exception {
		assertValidNumOfOnes(populationSize, numOfOnes);
		population = new ArrayList<Member>();
		for(int i=0; i < populationSize; i++) {
			Set<Integer> indexOfOnes = sampleRandomIndex(populationSize, numOfOnes);
			population.add(new Member(memberSize , indexOfOnes, code_handler));
		}
	}

	public boolean isEvaluated() {
		assertValidPopulation();
		for(Member m: population) {
			try {
				m.getFitnessValue();
			} catch (Exception e) {
				return false;
			} 
		}
		return true;	
	}
	
	public Member getFittestMember() {
		if(!this.isEvaluated())
			throw new FitnessValueNotComputedException();
		
		Member max = population.get(0);
		for(Member member: population) {
			if(member.getFitnessValue() > max.getFitnessValue()) {
				max = member;
			}
		}
		return max;
	}
	
	private void assertValidNumOfOnes(int populationSize, int numOfOnes) 
			throws IllegalArgumentException, NotInRangeNumberOfOnesException{
		if(populationSize < numOfOnes )
			throw new IllegalArgumentException("Requested more ones then "
					+ "possible, the \"numOfOnes\" must be "
					+ "lower then or equal to the \"populationSize\"");
		assertNumOfOnesInRange(numOfOnes);
	}

	private Set<Integer> sampleRandomIndex(int populationSize, int numOfOnes) {
		Set<Integer> indexOfOnes = new HashSet<Integer>();
		Random rand = new Random();
		while(indexOfOnes.size() < numOfOnes) {
			int higherIndex = memberSize - 1;
			int nextIndex = rand.nextInt(higherIndex);
			indexOfOnes.add(nextIndex);
		}
		return indexOfOnes;
	}
	
	public void setPoputation(List<Member> population) {
//		try {
//			assertValidPopulation(population);
//		} catch (NotInRangeNumberOfOnesException e) {
//			System.out.println("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
//			population = fixPopulation(population);
//		}
		this.population = population;
	}

	private List<Member> fixPopulation(List<Member> p) {
		List<Member> newMembers = new ArrayList<Member>();
		for(Member m: p) {
			try {
				assertNumOfOnesInRange(m.countOnes());
				newMembers.add(m);
			} catch (NotInRangeNumberOfOnesException e) {
				fixMethod(p, newMembers);
			}
		}
		return newMembers;
	}

	private void fixMethod(List<Member> p, List<Member> newMembers) {
		Random rand = new Random();
		int range = this.maxNumOfOnes - this.minNumOfOnes;
		int numOfOnes = rand.nextInt(range) + this.minNumOfOnes; 
		Set<Integer> indexOfOnes = sampleRandomIndex(p.size(), numOfOnes);
		newMembers.add(new Member(memberSize , indexOfOnes, this.code_handler));
	}

	public List<Member> getPopulation() {
		assertValidPopulation();
		return population;
	}
	
	/**
	 * @return the size of the population
	 * @throws Exception 
	 */
	public int getPopulationSize() {
		assertValidPopulation();
		return this.population.size();
	}

	
	/**
	 * @return the length of each bit string (Member),
	 * i.e. the number of methods worth considering of the system
	 */
	public int getBitsLength() {
		return memberSize;
	}

	private void assertValidPopulation() {
		if(this.population == null)
			throw new PopulationNotCreatedException();
	}
	
	private void assertValidPopulation(List<Member> p) throws NotInRangeNumberOfOnesException {
		if(p == null)
			throw new NullPointerException();
		if(p.isEmpty())
			throw new RuntimeException("Empty population");
		for(Member m: p) {
			assertNumOfOnesInRange(m.countOnes());
		}
	}
	
	private void assertNumOfOnesInRange(int numOfOnes) throws NotInRangeNumberOfOnesException {
		if(numOfOnes > this.maxNumOfOnes 
				|| numOfOnes < this.minNumOfOnes)
			throw new NotInRangeNumberOfOnesException(this.minNumOfOnes, this.maxNumOfOnes);
	}
}
