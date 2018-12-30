package pland.gamecore.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class MapInfo {

    @Getter @Setter
    private int MapId;

    @Getter @Setter
    private int weight;

    @Getter @Setter
    private int height;

    public boolean canGo(int x, int y) {
        return !(x < 0 || x > this.weight || y < 0 || y > this.height);
    }

}
