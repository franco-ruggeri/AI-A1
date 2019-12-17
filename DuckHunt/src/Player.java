import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

class Player {

	private static final Action cDontShoot = new Action(-1, -1);
	private static final int COUNT_PATTERN = 2;
	private static final double MIN_CONFIDENCE_SHOT = 0.82;
	private static final int MAX_ITERS_SHOT = 1000;
	private static final int MAX_ITERS_GUESS = 1000;
	private static final int TURNS_PER_ROUND = 100;
	private static final int TURN_TO_SHOOT_MULTI_PLAYER = 50;
	
	private int round, timeStep;
	private List<List<HMM>> speciesModels;
	private int hits, totShots, rightGuesses, totGuesses, lastGuess[];	// statistics
	
    public Player() {
    	round = -1;
    	speciesModels = new ArrayList<>(Constants.COUNT_SPECIES);
    	for (int i=0; i<Constants.COUNT_SPECIES; i++)
    		speciesModels.add(new LinkedList<>());
    	hits = totShots = rightGuesses = totGuesses = 0;
    }

    /**
     * Shoot!
     *
     * This is the function where you start your work.
     *
     * You will receive a variable pState, which contains information about all
     * birds, both dead and alive. Each bird contains all past moves.
     *
     * The state also contains the scores for all players and the number of
     * time steps elapsed since the last time this function was called.
     *
     * @param pState the GameState object with observations etc
     * @param pDue time before which we must have returned
     * @return the prediction of a bird we want to shoot at, or cDontShoot to pass
     */
    public Action shoot(GameState pState, Deadline pDue) {
        int nBirds = pState.getNumBirds();
        Action action = cDontShoot;
        double confidence = Double.NEGATIVE_INFINITY;
        
		// new round
		if (round != pState.getRound()) {
			round = pState.getRound();
			timeStep = 0;
		}

		// take your time to observe, don't rush
		timeStep++;
		int nPlayer = pState.getNumPlayers();
		if ((nPlayer == 1 && timeStep < TURNS_PER_ROUND - nBirds) || (nPlayer > 1 && timeStep < TURN_TO_SHOOT_MULTI_PLAYER))
			return action;
		
        for (int b=0; b<nBirds; b++) {
        	Bird bird = pState.getBird(b);
        	
        	// dead => cannot be shot down
        	if (bird.isDead())
        		continue;
        	
        	// don't shoot if you are not sure it's not a black stork
        	int[] observationSequence = getObservationSequence(bird);
        	int species = guessSpecies(observationSequence);
        	if (species == Constants.SPECIES_BLACK_STORK || species == Constants.SPECIES_UNKNOWN)
        		continue;
        	
        	// get useful models (i.e. birds of the same species + this bird)
        	List<HMM> models = new LinkedList<>(speciesModels.get(species));
        	HMM birdModel = new HMM(COUNT_PATTERN, Constants.COUNT_MOVE);	// this bird
        	birdModel.learn(observationSequence, MAX_ITERS_SHOT);
        	models.add(birdModel);
        	
        	// search most likely next move
        	for (HMM model : models) {
        		// get probability distribution of next move
        		double[] nextMoveDistribution = model.nextObservationDistribution(observationSequence);
        		
        		// update action
        		for (int m=0; m<nextMoveDistribution.length; m++) {
            		if (nextMoveDistribution[m] > confidence && nextMoveDistribution[m] > MIN_CONFIDENCE_SHOT) {
            			action = new Action(b, m);
            			confidence = nextMoveDistribution[m];
            		}
            	}
        	}
        }
        
        // statistics
        if (action != cDontShoot) {
        	totShots++;
        	System.err.println("Shot with confidence: " + confidence);
        }
        return action;
    }

    /**
     * Guess the species!
     * This function will be called at the end of each round, to give you
     * a chance to identify the species of the birds for extra points.
     *
     * Fill the vector with guesses for the all birds.
     * Use SPECIES_UNKNOWN to avoid guessing.
     *
     * @param pState the GameState object with observations etc
     * @param pDue time before which we must have returned
     * @return a vector with guesses for all the birds
     */
    public int[] guess(GameState pState, Deadline pDue) {
    	int nBirds = pState.getNumBirds();
        int[] lGuess = new int[nBirds];
        
        for (int b=0; b<nBirds; b++) {
        	// first round (no information), guess anyway to get information
        	if (round == 0) {
        		lGuess[b] = Constants.SPECIES_PIGEON;
        	} else {
        		// species recognition
        		lGuess[b] = guessSpecies(getObservationSequence(pState.getBird(b)));
        		
        		// none of the models fits, guess randomly to get information
        		if (lGuess[b] == Constants.SPECIES_UNKNOWN)
        			lGuess[b] = (int) (Math.random() * Constants.COUNT_SPECIES);
        	}
        	
        	// statistics
        	if (lGuess[b] != Constants.SPECIES_UNKNOWN)
        		totGuesses++;
        }
        
        lastGuess = lGuess;
        return lGuess;
    }

    /**
     * If you hit the bird you were trying to shoot, you will be notified
     * through this function.
     *
     * @param pState the GameState object with observations etc
     * @param pBird the bird you hit
     * @param pDue time before which we must have returned
     */
    public void hit(GameState pState, int pBird, Deadline pDue) {
        System.err.println("HIT BIRD!!!");
        hits++;		// statistics
    }

    /**
     * If you made any guesses, you will find out the true species of those
     * birds through this function.
     *
     * @param pState the GameState object with observations etc
     * @param pSpecies the vector with species
     * @param pDue time before which we must have returned
     */
    public void reveal(GameState pState, int[] pSpecies, Deadline pDue) {
    	for (int b=0; b<pSpecies.length; b++) {
    		if (pSpecies[b] == Constants.SPECIES_UNKNOWN)
    			continue;
    		
    		// add model of bird to collection of models of its species
    		HMM hmm = new HMM(COUNT_PATTERN, Constants.COUNT_MOVE);
    		hmm.learn(getObservationSequence(pState.getBird(b)), MAX_ITERS_GUESS);
    		speciesModels.get(pSpecies[b]).add(hmm);
    		
    		// statistics
    		if (pSpecies[b] == lastGuess[b])
    			rightGuesses++;
    	}
    	
    	// statistics
    	System.err.println();
    	System.err.println("HITS: " + hits);
    	System.err.println("TOT SHOTS: " + totShots);
    	System.err.println("RIGHT GUESSES: " + rightGuesses);
    	System.err.println("TOT GUESSES: " + totGuesses);
    	System.err.println();
    }
    
	/**
	 * Computes the most likely species the bird belongs to, solving the evaluation
	 * problem for different species and choosing the model that best fits the
	 * observations (species recognition).
	 * 
	 * @param observationSequence observation sequence
	 * @return most likely species
	 */
    private int guessSpecies(int[] observationSequence) {
    	int guess = Constants.SPECIES_UNKNOWN;
    	double maxConfidence = Double.NEGATIVE_INFINITY;
    	
    	for (int s=0; s<Constants.COUNT_SPECIES; s++) {
			for (HMM hmm : speciesModels.get(s)) {
				// evaluate observation sequence
				double confidence = hmm.evaluate(observationSequence);
				
				// update guess
				if (confidence > maxConfidence) {
					guess = s;
					maxConfidence = confidence;
				}
			}
		}
    	return guess;
    }
    
    /**
     * Gets the observation sequence (movements) of the bird.
     * 
     * @param bird bird
     * @return observation sequence
     */
    private int[] getObservationSequence(Bird bird) {
    	int[] observationSequence = new int[bird.getSeqLength()];
    	for (int t=0; t<observationSequence.length && bird.wasAlive(t); t++)
    		observationSequence[t] = bird.getObservation(t);
    	return observationSequence;
    }
}
