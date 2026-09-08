package uz.insonline.travel.Provider.payload.reponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uz.insonline.travel.commons.payload.response.ApiResponseAll;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationResponseDto extends ApiResponseAll {
    private String name;
    private String regCertificate;
    private String regCertificateIssueDate;
    private String email;
    private String address;
    private String phone;
    private String soato;
    private String district;
    private String oblast_id;
    private String rayon_id;
}
