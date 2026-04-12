package models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ApiClientRequest {

    @JsonProperty("clientName")
    private String clientName;

    @JsonProperty("clientEmail")
    private String clientEmail;
}