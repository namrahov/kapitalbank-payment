package com.kapitalbank.payment.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailDto implements Serializable {

    @Serial
    private static final long serialVersionUID = -8481081261148363197L;

    private String from;
    private String to;
    private String subject;
    private String body;

}
