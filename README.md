I will log here what I think it needs to be done in further iterations:


 1. Integrate with postgresql. Right now it's using a lightweight database for testing only.
 2. Figure out a good scheme for filesystem, so we don't need access database for everything
 3. Create an in memory data structure for jobs that allows prioritization and multiplexing on computationID
    The jobs class should take care of this.
        Alternatively, pick a job at random (I personally think that this is the best solution)

4. Build on the computation class and create parallelization for the prime number example only
5. Create a "user" interface for creating Computations so that we can test the application properly


6. We should start integrating the service soon.

