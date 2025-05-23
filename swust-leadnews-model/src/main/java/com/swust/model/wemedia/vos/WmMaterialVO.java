package com.swust.model.wemedia.vos;

import com.swust.model.wemedia.pojos.WmMaterial;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class WmMaterialVO extends WmMaterial {
    private String author;
    private String channelName;
}
