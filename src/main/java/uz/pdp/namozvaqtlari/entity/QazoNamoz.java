package uz.pdp.namozvaqtlari.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class QazoNamoz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne
    private Users users;
    private int bomdod=0;
    private int peshin=0;
    private int asr=0;
    private int shom=0;
    private int xufton=0;

    public Integer getValue(String namoz) {
        switch (namoz) {
            case "bomdod": return getBomdod();
            case "peshin": return getPeshin();
            case "asr": return getAsr();
            case "shom": return getShom();
            case "xufton": return getXufton();
            default: return 0; // Noto‘g‘ri qiymat kelib qolsa, 0 qaytaradi
        }
    }

}
