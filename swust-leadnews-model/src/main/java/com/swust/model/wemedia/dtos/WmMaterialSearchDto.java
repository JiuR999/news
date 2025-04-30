package com.swust.model.wemedia.dtos;

import com.swust.model.wemedia.pojos.WmMaterial;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class WmMaterialSearchDto extends WmMaterial {
    private String fileContent;
}
