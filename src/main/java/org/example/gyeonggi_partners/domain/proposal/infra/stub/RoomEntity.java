package org.example.gyeonggi_partners.domain.proposal.infra.stub;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.gyeonggi_partners.domain.proposal.infra.persistence.ProposalEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoomEntity {


    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "room")
    private List<ProposalEntity> proposal = new ArrayList<>();


    public RoomEntity(Long id) {
        this.id = id;
    }
}
