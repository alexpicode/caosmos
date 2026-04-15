package com.caosmos.directors.domain.model;

import java.util.List;
import lombok.Data;

@Data
public class ItemTemplate {

  private String name;
  private List<String> tags;
  private String category;
  private Double radius;
  private Double width;
  private Double length;
  private Double amount;
}
