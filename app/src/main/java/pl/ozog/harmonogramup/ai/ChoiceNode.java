package pl.ozog.harmonogramup.ai;

import java.util.LinkedHashMap;
import java.util.Map;

public class ChoiceNode {
    String dataType;
    String id;
    String name;
    Map<String, ChoiceNode> childs;

    public ChoiceNode(String dataType, String id, String name) {
        this.dataType = dataType;
        this.id = id;
        this.name = name;
        this.childs = new LinkedHashMap<>();
    }

    public void addChild(String key, ChoiceNode node){
        childs.put(key, node);
    }
    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, ChoiceNode> getChilds() {
        return childs;
    }

    public void setChilds(Map<String, ChoiceNode> childs) {
        this.childs = childs;
    }
}
