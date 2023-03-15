package pl.ozog.harmonogramup;

public class RangeItem {
    String range;
    String semestrId;
    String text;
    boolean selected;

    public RangeItem(String range, String semestrId, String text, boolean selected) {
        this.range = range;
        this.semestrId = semestrId;
        this.text = text;
        this.selected = selected;
    }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }

    public String getSemestrId() {
        return semestrId;
    }

    public void setSemestrId(String semestrId) {
        this.semestrId = semestrId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
