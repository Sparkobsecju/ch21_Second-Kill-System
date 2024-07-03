package com.example.ch21.persistence.entity;

import lombok.*;

import java.io.Serializable;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class SeckillOrderPK implements Serializable {
    private static final long serialVersionUID = -6513255139439774184L;
    private Integer itemId;
    private String mobile;
}
