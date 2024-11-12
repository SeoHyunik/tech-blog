package com.automatic.tech_blog.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.sql.Timestamp;
import lombok.Data;

@Entity
@Table(name = "tb_md_files")
@Data
public class TbMdFiles {
  @Id
  private int id;
  private String fileName;
  private String filePath;
  private Timestamp createdAt;
  private Timestamp modifiedAt;
}
