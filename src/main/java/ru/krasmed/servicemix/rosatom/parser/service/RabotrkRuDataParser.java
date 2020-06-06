package ru.krasmed.servicemix.rosatom.parser.service;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.math.BigInteger;
import java.util.HashMap;


public class RabotrkRuDataParser extends DefaultHandler {
    enum Type {INT, STRING }
    private MapSqlParameterSource paramFiles;
    private MapSqlParameterSource paramPosts;
    private MapSqlParameterSource paramInstructions;
    private MapSqlParameterSource paramInstructionsContent;
    private MapSqlParameterSource paramItem;
    private String idPost;
    private DataManager dataManager;
    private String step ="false";
    private String idInstruction;
    private Integer currentSection;
    private Integer currentLevel;
    private String currentLi;

    static final HashMap<String, Type> MAP_INSERT_FILE = new HashMap<String, Type>(){{
        put("id", Type.INT);
        put("id_post", Type.INT);
        put("filename", Type.STRING);
    }};

    static final HashMap<String, Type> MAP_INSERT_POST = new HashMap<String, Type>(){{
        put("id", Type.INT);
        put("name", Type.STRING);
    }};

    static final HashMap<String, Type> MAP_INSERT_INSTRUCTION = new HashMap<String, Type>(){{
        put("id", Type.INT);
        put("id_post", Type.INT);
    }};

    static final HashMap<String, Type> MAP_INSERT_INSTRUCTION_CONTENT = new HashMap<String, Type>(){{
        put("id", Type.INT);
        put("id_instruction", Type.INT);
        put("id_item", Type.INT);
        put("id_section", Type.INT);
    }};


    static final HashMap<String, Type> MAP_INSERT_ITEM = new HashMap<String, Type>(){{
        put("id", Type.INT);
        put("name", Type.STRING);
    }};

    //Конструктор
    public RabotrkRuDataParser(DataManager dataManager, String url) throws SAXException {
        this.dataManager=dataManager;
        this.paramFiles = new MapSqlParameterSource();
        this.paramPosts = new MapSqlParameterSource();
        this.paramInstructions = new MapSqlParameterSource();
        this.paramInstructionsContent = new MapSqlParameterSource();
        this.paramItem = new MapSqlParameterSource();
        addTypedValue(MAP_INSERT_FILE,paramFiles,"id",this.dataManager.getNextwal("files_id_seq").toString());
        addTypedValue(MAP_INSERT_FILE,paramFiles,"filename",url);
        this.idPost=this.dataManager.getNextwal("posts_id_seq").toString();
        addTypedValue(MAP_INSERT_FILE,paramFiles,"id_post",this.idPost);
        this.currentSection = 0;
        this.currentLevel=0;
    }

    /*
     Добавление в параметров запроса с проверкой с проверкой
     */
    private void addTypedValue(HashMap<String, Type> map, MapSqlParameterSource param, String key, String val) throws SAXException {
            switch (map.get(key)) {
                case INT:
                    param.addValue(key, val != null ? Integer.valueOf(val) : null);
                    break;
                case STRING:
                    param.addValue(key, val);
                    break;
                default:
                    throw new SAXException("Значение атрибута " + key + " не определено");
            }
    }


    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        try {
            switch (qName) {
                case "h1":
                    this.step = "post";
                    break;
                case "h2":
                    this.step = "section";
                    break;
                case "ol" :
                    this.currentLevel++;
                    break;
                case "ul" :
                    this.currentLevel++;
                    break;

                case "li":
                    this.step = "li";
                    break;
            }
        }
        catch (Exception e) {
            //
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        switch (this.step) {
            case "post" :
                addTypedValue(MAP_INSERT_POST,paramPosts,"id", this.idPost);
                addTypedValue(MAP_INSERT_POST,paramPosts,"name",new String(ch, start, length));
                //System.out.println(paramPosts.toString());
                //insert post
                this.dataManager.insertPost(paramPosts);
                //System.out.println(paramFiles.toString());
                //insertfile
                this.dataManager.insertFile(paramFiles);
                addTypedValue(MAP_INSERT_INSTRUCTION,paramInstructions,"id_post", this.idPost);
                this.idInstruction=this.dataManager.getNextwal("instructions_id_seq").toString();
                addTypedValue(MAP_INSERT_INSTRUCTION,paramInstructions,"id", this.idInstruction);
                //insert instruction
                //System.out.println(paramInstructions.toString());
                this.dataManager.insertInstruction(paramInstructions);
                this.step="false";
                break;
            case "section" :
                String sect=new String(ch, start, length);
                sect=sect.toUpperCase();
                if (sect.indexOf("ОБЩИЕ") !=-1? true: false) {
                    this.currentSection=1;
                } else if (sect.indexOf("ОБЯЗАН") !=-1? true: false) {
                    this.currentSection=2;
                } else if (sect.indexOf("ПРАВ") !=-1? true: false) {
                    this.currentSection=3;
                } else if (sect.indexOf("ОТВЕТ") !=-1? true: false) {
                    this.currentSection=4;
                }
                //System.out.println(this.currentSection.toString());

                this.step="false";
                break;
            case "li" :
                String itm = new String(ch, start, length);
                this.currentLi=itm;
                break;
        }
    }
    @Override
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        switch (qName) {
            case "h2" :
                this.step="false";
                break;
            //case "article" :
                //throw new SAXException("Конец");
               // break;
            case "ol" :
                this.currentLevel--;
                break;
            case "ul" :
                this.currentLevel--;
                break;
            case "li" :
                if (this.currentLi!="") {
                    //this.currentLi = this.currentLi.replaceAll("\\^[0-9.\\-\\s]+", "");
                    //System.out.println(this.currentLi);
                    String idItem=this.dataManager.getNextwal("items_id_seq").toString();
                    addTypedValue(MAP_INSERT_ITEM,paramItem,"id", idItem);
                    addTypedValue(MAP_INSERT_ITEM,paramItem,"name", this.currentLi);
                    this.dataManager.insertItems(paramItem);
                    String idInstCont=this.dataManager.getNextwal("instructions_content_id_seq").toString();
                    addTypedValue(MAP_INSERT_INSTRUCTION_CONTENT,paramInstructionsContent,"id", idInstCont);
                    addTypedValue(MAP_INSERT_INSTRUCTION_CONTENT,paramInstructionsContent,"id_section", this.currentSection.toString());
                    addTypedValue(MAP_INSERT_INSTRUCTION_CONTENT,paramInstructionsContent,"id_item", idItem);
                    addTypedValue(MAP_INSERT_INSTRUCTION_CONTENT,paramInstructionsContent,"id_instruction", this.idInstruction);
                    this.dataManager.insertInstructionContent(paramInstructionsContent);
                }

                this.currentLi="";
                this.step = "false";
                break;
        }
    }
}
