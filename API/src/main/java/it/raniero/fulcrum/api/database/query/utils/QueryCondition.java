package it.raniero.fulcrum.api.database.query.utils;

public class QueryCondition {

    private StringBuilder condition;

    public QueryCondition(String condition) {
        this.condition = new StringBuilder(condition);
    }

    public QueryCondition and(String cond) {
        condition.append(" AND ").append(cond);
        return this;
    }

    public QueryCondition or(String cond) {
        condition.append(" OR ").append(cond);
        return this;
    }

    public String getCondition() {
        return condition.toString();
    }
}
