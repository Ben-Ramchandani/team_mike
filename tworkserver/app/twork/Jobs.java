package twork;

import java.util.List;
import java.util.Random;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.ebean.RawSql;
import com.avaje.ebean.RawSqlBuilder;
import com.avaje.ebean.SqlQuery;

import models.Computation;
import models.Job;

public class Jobs {
	//Also singleton
	
	//Make a heap structure here such that it allows both searching through all the jobs in the program and also based on a list of jobs
	
	public static Job getJob(int priority, List<Long> computations) {
		/*
		 * QUERY FOR MORE JOBS AT ONCE
		 * ORDER BY PRIORITY
		 */
		Random r = new Random();
		Long computationID = computations.get(r.nextInt(computations.size()));
		
		String selectRandomJob = 
				"SELECT * FROM all_jobs WHERE computationID ="  + computationID.toString()
				+ " ORDER BY RANDOM()"
				+ " LIMIT 1";
		
		RawSql selectRandomJobSql = RawSqlBuilder.parse(selectRandomJob).create();
		
		Query<Job> query = Ebean.find(Job.class);
		query.setRawSql(selectRandomJobSql);
		
		List<Job> l = query.findList();
		if (l.isEmpty()) return null;
		return l.get(0);
	}
	
	
	public static Job getJob(int priority) {
		return null;
	}
	
}
