/**
 * --------------------------------------------------------------------------
 *                                 SBML4j
 * --------------------------------------------------------------------------
 * University of Tuebingen, 2022.
 * 
 * This code is part of the SBML4j software package and subject to the terms
 * and conditions defined by its license (MIT License). For license details
 * please refer to the LICENSE file included as part of this source code
 * package.
 * 
 * For a full list of authors, please refer to the file AUTHORS.
 */
package org.sbml4j;

import org.sbml4j.config.DataSourceConfig;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
@ComponentScan(basePackages = { "org.sbml4j", "org.sbml4j.api",
		 "io.swagger.configuration"})
public class Sbml4jApplication {
	@Autowired
	DataSourceConfig dataSourceConfig;

	public static void main(String[] args) {
		try {
			SpringApplication.run(Sbml4jApplication.class, args);
		} catch (UnsatisfiedDependencyException e) {
			System.err.println("Failed to start SBML4j, UnsatisfiedDependencyException encounterd. This could be due to the database not started yet. If you started this in docker-compose, we will try to start again...");
			System.exit(1);
		} catch (Exception e) {
			System.err.print("Encountered exception " + e.getMessage() + ". Trying to restart..");
			System.exit(2);
		}
	}
	
}
