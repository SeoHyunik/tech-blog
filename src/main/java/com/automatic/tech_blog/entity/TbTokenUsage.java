package com.automatic.tech_blog.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.Data;

@Entity
@Table(name = "tb_token_usage")
@Data
public class TbTokenUsage {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @Column(unique = true, nullable = false)
  private String fileId;

  @Column(nullable = false)
  private int inputTokens;

  @Column(nullable = false)
  private int outputTokens;

  @Column(nullable = false)
  private BigDecimal convertedKrw;

  @Column
  private BigDecimal profitKrw;

  @Column(nullable = false)
  private String model;
}
