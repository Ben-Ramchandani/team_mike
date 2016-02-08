package computations;

import java.util.UUID;

//This generates a simple computation, like the prime class, where all the input and results can fit in memory.
//This class MUST have NO STATE whatsoever.
public interface BasicComputationGenerator {
	//Should have a default constructor.
	
	//Generate jobs and place them in the database
	//Return the Computation UUID
	public UUID generateComputation(String input);
	
	//Once the computation finishes, call this to collate the results
	//UUID of Computation
	//Should NOT make changes to db
	public String getResult(UUID computationID);
}
