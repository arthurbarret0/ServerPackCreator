package de.griefed.serverpackcreator.web.data

import jakarta.persistence.*
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import org.hibernate.annotations.JdbcTypeCode
import java.sql.Types

@Entity
class FileData {

    @Id
    @GeneratedValue
    @Column(updatable = false, nullable = false)
    var id: Int = 0

    @Lob
    @Fetch(FetchMode.SELECT)
    @Basic(fetch = FetchType.LAZY)
    @JdbcTypeCode(Types.BINARY)
    var data: ByteArray? = null

    constructor()

    constructor(data: ByteArray?) {
        this.data = data
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FileData

        if (data != null) {
            if (other.data == null) return false
            if (!data.contentEquals(other.data)) return false
        } else if (other.data != null) return false

        return true
    }

    override fun hashCode(): Int {
        return data?.contentHashCode() ?: 0
    }

    override fun toString(): String {
        return "FileData(id=$id, data=${data?.contentToString()})"
    }
}