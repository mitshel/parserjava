package ru.krasmed.servicemix.rosatom.parser.service;

import org.apache.camel.BeanInject;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.jdbc.core.JdbcTemplate;

import org.apache.commons.io.IOUtils;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;



public class ServiceProcessor implements Processor {
    @BeanInject("jdbcTemplate")
    private JdbcTemplate jdbcTemplate;
    public static SimpleDateFormat dateFormatGet= new SimpleDateFormat("dd/MM/yyyy");

    @Override
    public void process(Exchange exchange) throws Exception {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();
        for (Integer i = 1; i<=393; i++) {
            System.out.println(i.toString());
            String url = "https://www.rabotka.ru/job_description/" + new BigDecimal(i).toString() + ".php";
            RabotrkRuDataParser saxp = new RabotrkRuDataParser(new DataManager(jdbcTemplate), url);


            InputStream stream = new URL(url).openStream();
            StringWriter writer = new StringWriter();
            IOUtils.copy(stream, writer, "UTF-8");
            String streamString = writer.toString();
            streamString=streamString.replace("<br>","");
            streamString=streamString.replace("<br/>","");
            streamString=streamString.replace("&nbsp;"," ");
            streamString=streamString.replace("&mdash;","");
            streamString=streamString.replace("&laquo;","");
            streamString=streamString.replace("&raquo;","");

            streamString=streamString.replace("II."," ");
            streamString=streamString.replace("I."," ");
            streamString=streamString.replace("III."," ");
            streamString=streamString.replace("IV."," ");
            streamString=streamString.replace("V."," ");
            streamString=streamString.replace("VI."," ");


            //streamString=streamString.replace("<link rel=\"stylesheet\" href=\"/css/style-2015.css\">","");
            //streamString=streamString.replace("<meta name=\"keywords\" content=\"\" />","");
            //streamString=streamString.replace("<meta name=\"description\" content=\"\" />","");

            streamString = streamString.substring(streamString.indexOf("<article class=\"post\">"));
            streamString = streamString.substring(0, streamString.indexOf("</article>"));
            streamString=streamString.trim()+"</article>";


            InputStream newStream = new ByteArrayInputStream(streamString.getBytes(StandardCharsets.UTF_8));
            parser.parse(newStream, saxp);
        }
    }
}
