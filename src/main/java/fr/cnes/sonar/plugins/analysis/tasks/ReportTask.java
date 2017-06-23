package fr.cnes.sonar.plugins.analysis.tasks;

import org.sonar.api.server.ws.Request;
import org.sonar.api.server.ws.Response;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static fr.cnes.sonar.plugins.analysis.utils.StringManager.*;

/**
 * Execute element to produce the report
 * @author garconb
 */
public class ReportTask extends AbstractTask {

    /**
     * Product the report
     * @param projectId Key of the project to report
     * @param projectQualityGate Quality Gate of the project to report
     * @param projectName  Name of the project to report
     * @param reportAuthor Author of the report
     * @param reportPath Output folder
     * @param reportTemplate template to use for the processing
     * @param issuesTemplate template for the xlsx file
     * @return logs of the task
     * @throws IOException when a file writing goes wrong
     * @throws InterruptedException when a command is not finished
     */
    public String report(String projectId, String projectQualityGate,
                         String projectName, String reportAuthor, String reportPath,
                         String reportTemplate, String issuesTemplate) throws IOException, InterruptedException {

        // creation of the output directory
        boolean success = (new File(reportPath)).mkdirs();
        if (!success) {
            // Directory creation failed
            log(String.format(string(CNES_MKDIR_ERROR), reportPath));
        }
        // formatted date
        String date = new SimpleDateFormat(string(DATE_PATTERN)).format(new Date());
        // construct the command string to run analysis
        String command = String.format(string(CNES_COMMAND_REPORT),
                string(CNES_REPORT_PATH), string(SONAR_URL), projectId, projectQualityGate,
                projectName, reportAuthor, date, reportPath, reportTemplate, issuesTemplate);
        // log the command used
        log(command);
        // log the execution result
        log(executeCommand(command));

        // return the log
        return getLogs();
    }

    /**
     * Use the user's request to start the report generation
     * @param request request coming from the user
     * @param response response to send to the user
     * @throws IOException when an error occurred on file writing
     * @throws InterruptedException ...
     */
    @Override
    public void handle(Request request, Response response) throws IOException, InterruptedException {
        // reset logs to not stack them
        setLogs("");

        // Name of the project provided by the user through parameters
        final String projectName = request.mandatoryParam(string(CNES_ACTION_2_PARAM_3_NAME));
        // Date of today
        final String today = new SimpleDateFormat(string(DATE_PATTERN)).format(new Date());
        // Construct the name of the output folder like that: sharedFolder/project-date-results
        final String output = String.format(string(CNES_REPORTS_FOLDER),string(CNES_REPORTER_OUTPUT), projectName, today);

        // read request parameters and generates response output
        // generate the reports and save output
        String result = report(
                request.mandatoryParam(string(CNES_ACTION_2_PARAM_1_NAME)),
                request.mandatoryParam(string(CNES_ACTION_2_PARAM_2_NAME)),
                projectName,
                request.mandatoryParam(string(CNES_ACTION_2_PARAM_4_NAME)),
                output,
                string(CNES_REPORTER_TEMPLATE),
                string(CNES_ISSUES_TEMPLATE)
        );

        // set the response
        response.newJsonWriter()
                .beginObject()
                // add logs to response
                .prop(string(CNES_ACTION_2_FIELD_1), result)
                .endObject()
                .close();
    }
}
