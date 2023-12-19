package de.griefed.serverpackcreator.web.data

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Lob
import org.hibernate.annotations.JdbcTypeCode
import java.math.BigInteger
import java.sql.Types

@Entity
class StorageData {

    @Id
    @Column(updatable = false, nullable = false)
    var id: Long

    @Column
    var hash: BigInteger

    @Lob
    @JdbcTypeCode(Types.BINARY)
    var data: ByteArray

    constructor(id: Long, hash: BigInteger, data: ByteArray) {
        this.id = id
        this.hash = hash
        this.data = data
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StorageData

        return hash == other.hash
    }

    override fun hashCode(): Int {
        return hash.hashCode()
    }

    override fun toString(): String {
        return "FileData(id=$id, hash=$hash, data=${data.contentToString()})"
    }


}