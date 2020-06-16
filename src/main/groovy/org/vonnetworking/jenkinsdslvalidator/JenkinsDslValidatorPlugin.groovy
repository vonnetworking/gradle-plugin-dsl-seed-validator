package org.vonnetworking.jenkinsdslvalidator;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class JenkinsDslValidatorPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
      String patterns = '''curl.*-v.*
.*curl.*-j
'''
      def file = new File("prohibited.patterns")
      file.withWriter('UTF-8') { writer ->
        writer.write(patterns)
      }
      // project.extensions.create("jobFileValidator", JobFileValidatorExtension)

      //Write the jobComplianceSpec.groovy file out to test directory so thaty it is updated to the latest version
      ClassLoader loader = getClass().getClassLoader();
      InputStream inputStream = loader.getResourceAsStream("test/JobComplianceSpec.groovy")

      File outputFile = new File('src/test/groovy/com/dsl/JobComplianceSpec.groovy')
      outputFile.newWriter().withWriter { w ->
        w << inputStream.text
      }

      inputStream.close();
      /* copy {
        from(project.fileTree(loader.getResource("test")))
        into("src/test/groovy/com/dsl")
      }*/
    }
}
