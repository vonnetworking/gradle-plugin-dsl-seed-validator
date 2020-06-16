package com.dslexample

import com.dslexample.support.TestUtil
import hudson.model.Item
import hudson.model.View
import javaposse.jobdsl.dsl.DslScriptLoader
import javaposse.jobdsl.dsl.GeneratedItems
import javaposse.jobdsl.dsl.GeneratedJob
import javaposse.jobdsl.dsl.GeneratedView
import javaposse.jobdsl.dsl.JobManagement
import javaposse.jobdsl.plugin.JenkinsJobManagement
import jenkins.model.Jenkins
import org.junit.ClassRule
import org.jvnet.hudson.test.JenkinsRule
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Tests that all dsl scripts in the jobs directory do not contain any restricted strings / commands
 *
 * This runs against the jenkins test harness. Plugins providing auto-generated DSL must be added to the build dependencies.
 */
class JobComplianceSpec extends Specification {

    @Shared
    @ClassRule
    private JenkinsRule jenkinsRule = new JenkinsRule()

    @Unroll
    void 'Run a series of standard validations on #file.name'(File file) {
        when:
            def testsOk = JobFileValidator.Check(file)
        then:
          assert testsOk // returns true when things work
        where:
            file << TestUtil.getJobFiles()
    }
}

public class JobFileValidator {
  static Boolean Check(File jobFile) {
    def result = true; //assume the job is good

    // --- Start Standard Pattern Check ---
    // check that the file contains required patterns so that overrides can be correctly applied
    def requiredElems = ['''String jobFolder =.*''',
                         '''String jobName =.*''',
                         '''String jobDescription = .*''',
                         '''folder\\(jobFolder\\).*''',
                         '''.*description jobDescription.*''',
                         '''job\\("\\$jobFolder/\\$jobName"\\).*''']
     requiredElems.each { patternStr -> //check that files contain necessary data elements
      def pattern = ~patternStr
      def patternMatches = pattern.matcher(jobFile.text)
      if (patternMatches.getCount() != 1) { //make sure each string is matched once and only once
          println "WARNING - Missing required line '$pattern' in file $jobFile"
          println "WARNING -     $jobFile will be excluded from seed job"
          result = false;
      }
    }
    // --- End Standard Pattern Check ---

    // --- Start Prohibited String Check ---
    // Validate that no prohibited strings are found in the job files
    def prohibitedPatterns = ['curl.*-v.*',
                              '.*curl.*-j.*',
                              '.*rm -rf.*',
                              '.*rm -fr.*']
    prohibitedPatterns.each { p ->
        jobFile.eachLine { l ->
            if (l =~ /${p}/) {
                println "WARNING - Prohibited Pattern '$p' found in $jobFile!"
                println "WARNING -     $jobFile will be excluded from seed job"
                result = false;
            }
        }
    }
    // --- End Prohibited String Check ---

    return result;
  } // end Check method
} // end class JobFileValidatorExtension
