package com.gpxmanager.csv;

import com.google.gson.annotations.SerializedName;

// Unused: Reactions.csv list the kudos I gave, not the one I received
public class Reaction {
  private final static String REACTION_TYPE = "Kudos";
  private final static String REACTION_PARENT_TYPE = "Activity";

  @SerializedName("date")
  private String date;
  @SerializedName("type")
  private String type;
  @SerializedName("parent_type")
  private String parentType;
  @SerializedName("parent_id")
  private String parentId;

  public String getDate() {
    return date;
  }

  public void setDate(String date) {
    this.date = date;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getParentType() {
    return parentType;
  }

  public void setParentType(String parentType) {
    this.parentType = parentType;
  }

  public String getParentId() {
    return parentId;
  }

  public void setParentId(String parentId) {
    this.parentId = parentId;
  }

  public boolean isTypeKudos() {
    return REACTION_TYPE.equalsIgnoreCase(type);
  }

  public boolean isParentTypeActivity() {
    return REACTION_PARENT_TYPE.equalsIgnoreCase(parentType);
  }
}
