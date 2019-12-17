import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

class Player {

	private static final Action cDontShoot = new Action(-1, -1);
	private static final int COUNT_PATTERN_SHOT = 5;
	private static final int COUNT_PATTERN_GUESS = 1;
	private static final double MIN_CONFIDENCE_SHOT = 0.75;
	private static final int TURNS_PER_ROUND = 100;
	private static final int MAX_ITERS_SHOT = 7000;
	private static final int MAX_ITERS_GUESS = 7000;
	
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

		/*
		 * Take your time to observe, don't rush.
		 * 
		 * N.B. timeStep < 2 because Baum-Welch algorithm needs at least 2 observations
		 * to work correctly (see implementation).
		 */
		timeStep++;
		if (timeStep < 2 || timeStep < TURNS_PER_ROUND - pState.getNumPlayers() * nBirds)
			return action;
		
        for (int b=0; b<nBirds; b++) {
        	Bird bird = pState.getBird(b);
        	
        	// dead => cannot be shot down
        	if (bird.isDead())
        		continue;
        	
        	// don't shoot if you guess it's a black stork
        	int[] observationSequence = getObservationSequence(bird);
        	int species = guessSpecies(observationSequence);
        	if (species == Constants.SPECIES_BLACK_STORK)
        		continue;
        	
        	// get probability distribution of next move
        	HMM hmm = new HMM(COUNT_PATTERN_SHOT, Constants.COUNT_MOVE);
        	hmm.learn(observationSequence, MAX_ITERS_SHOT);
        	double[] nextMoveDistribution = hmm.nextObservationDistribution(observationSequence);
        	
        	// update action
        	for (int m=0; m<nextMoveDistribution.length; m++) {
        		if (nextMoveDistribution[m] > confidence && nextMoveDistribution[m] > MIN_CONFIDENCE_SHOT) {
        			action = new Action(b, m);
        			confidence = nextMoveDistribution[m];
        		}
        	}
        	
//        	// prova
//        	if (species == Constants.SPECIES_UNKNOWN)
//        		species = Constants.SPECIES_PIGEON;
//        	HMM hmm = new HMM(COUNT_PATTERN_SHOT, Constants.COUNT_MOVE);
//        	hmm.learn(observationSequence, MAX_ITERS_SHOT);
//        	List<HMM> _speciesModels = speciesModels.get(species); 
//        	_speciesModels.add(hmm);
//        	
//        	int[] _observationSequence = new int[observationSequence.length + 1];
//        	for (int t=0; t<observationSequence.length; t++)
//        		_observationSequence[t] = observationSequence[t];
//        	
//        	for (int m=0; m<Constants.COUNT_MOVE; m++) {
//        		_observationSequence[observationSequence.length] = m;
//        		for (HMM h : _speciesModels) {
//	        		double tmp = h.evaluate(_observationSequence) - h.evaluate(observationSequence);
//	        		if (tmp > confidence) {
//	        			action = new Action(b, m);
//	        			confidence = tmp;
//	        		}
//        		}
//        	}
//        	_speciesModels.remove(hmm);
        }
        
        // statistics
        if (action != cDontShoot) {
        	totShots++;
//        	System.err.println("Shot with confidence: " + confidence);
        }
        return action;
        
        
        
        
        
        
        
        
        
        
        
        
//        /*
//         * Here you should write your clever algorithms to get the best action.
//         * This skeleton never shoots.
//         */
//        if (pState.getRound() != round) {
//            round = pState.getRound();
//        }
//
//        
//        double maxProb = Double.NEGATIVE_INFINITY;
//        int birdToAim = -1;
//        int moveToAim = -1;
//        
//        for(int i = 0; i < pState.getNumBirds(); i++) {
//            Bird bird = pState.getBird(i);
//            if(bird.isAlive()) {
//                int[] observations = getObservationSequence(bird);
//                if (observations.length > 100 - pState.getNumBirds()){
//                	HMM birdModel = new HMM(COUNT_PATTERN_SHOT, Constants.COUNT_MOVE);
//                    birdModel.learn(observations, MAX_ITERS_SHOT);
//                    int birdType = guessSpecies(observations);
//                    if (birdType == Constants.SPECIES_UNKNOWN)
//                    	birdType = Constants.SPECIES_PIGEON;
//                    if (birdType != Constants.SPECIES_BLACK_STORK) {
//                    	List<HMM> _speciesModels = speciesModels.get(birdType); 
//                    	_speciesModels.add(birdModel);
//                        
//                        
////                        tuple = guessMovement(observations, specieModels[birdType==-1?0:birdType]);
////                        int moveGuessed = (int) tuple[0];
////                        double probMove = tuple[1];
////                        if (probMove > maxProb) {
////                            moveToAim = moveGuessed;
////                            maxProb = probMove;
////                            birdToAim = i;
////                        }
//                        
//                        int[] _observationSequence = new int[observations.length + 1];
//                    	for (int t=0; t<observations.length; t++)
//                    		_observationSequence[t] = observations[t];
//                    	
//                    	for (int m=0; m<Constants.COUNT_MOVE; m++) {
//                    		_observationSequence[observations.length] = m;
//                    		for (HMM h : _speciesModels) {
//                        		double tmp = h.evaluate(_observationSequence) - h.evaluate(observations);
//                        		if (tmp > maxProb) {
//                        			birdToAim = i;
//                        			moveToAim = m;
//                        			maxProb = tmp;
//                        		}
//                    		}
//                    	}
//                        
//                        
//                        _speciesModels.remove(birdModel);
//                    } 
//                }
//            }
//        }
//        
//        // This line chooses not to shoot.
//        if(birdToAim == -1) {
//            return cDontShoot;
//        }
//        System.err.println("Disparo en la ronda " + pState.getRound() + " a " + birdToAim + "(Prob: " + maxProb + ")");
//        return new Action(birdToAim, moveToAim);
//
//        // This line would predict that bird 0 will move right and shoot at it.
//        // return Action(0, MOVE_RIGHT);
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
        	// first round => guess randomly to get information
        	if (round == 0)
//                lGuess[b] = (int) (Math.random() * Constants.COUNT_SPECIES);
        		lGuess[b] = Constants.SPECIES_PIGEON;
        	else
        		lGuess[b] = guessSpecies(getObservationSequence(pState.getBird(b)));
        	
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
    		HMM hmm = new HMM(COUNT_PATTERN_GUESS, Constants.COUNT_MOVE);
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
    
    private int[] getObservationSequence(Bird bird) {
    	int[] observationSequence = new int[bird.getSeqLength()];
    	for (int t=0; t<observationSequence.length && bird.wasAlive(t); t++)
    		observationSequence[t] = bird.getObservation(t);
    	return observationSequence;
    }
}
