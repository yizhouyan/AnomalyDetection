package selector.common.utils

import org.apache.commons.cli.{BasicParser, CommandLine, CommandLineParser, HelpFormatter, Option, Options, ParseException}

/**
  * Created by yizhouyan on 9/6/19.
  */
class ConfigParser {
    var confFile :String = ""
    var jsonFile: String = ""
    def this(args: Array[String]) = {
        this()
        parseCommandLine(args)
    }
    def parseCommandLine(args: Array[String]): Unit = {
        val options: Options = createCommandLineOptions()
        val commandLineParser:CommandLineParser = new BasicParser()
        var commandLine: CommandLine = null
        val helpFormatter = new HelpFormatter()
        try {
            commandLine = commandLineParser.parse(options, args)
        }
        catch {
            case ex: ParseException => {
                ex.printStackTrace()
                helpFormatter.printHelp("utility-name", options)
                System.exit(1)
            }
        }
        confFile = commandLine.getOptionValue("conf")
        println("Input Property File: " + confFile)
        jsonFile = commandLine.getOptionValue("json")
        println("Input Json File: " + jsonFile)
    }

    private def createCommandLineOptions(): Options = {
        val options: Options = new Options()
        var configFile:Option = new Option("c", "conf", true, "configuration file path")
        configFile.setRequired(false)
        configFile.setArgName("CONFIG FILE PATH")
        options.addOption(configFile)

        var jsonFile:Option = new Option("j", "json", true, "workflow json file path")
        jsonFile.setRequired(true)
        jsonFile.setArgName("WORKFLOW JSON FILE PATH")
        options.addOption(jsonFile)

        options
    }
}
