package co.payrail.nddc_assessment.integration.models.response;

import lombok.Builder;
import lombok.Data;

/**
 * @author Rasaq Agbalaya
 * @project branchless
 */
@Data
@Builder
public class BVNResponse {

    private String firstName;
    private String lastname;
    private String otherName;
    private String bvn;
    private String phoneNumber;
    private String dob;
    private String error;

}
