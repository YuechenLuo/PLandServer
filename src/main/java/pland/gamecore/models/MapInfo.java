package pland.gamecore.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class MapInfo {

    @Getter @Setter
    private byte MapId;

    @Getter @Setter
    private int weight;

    @Getter @Setter
    private int height;

}
