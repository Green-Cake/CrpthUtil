package crpth.util.io

import java.io.DataOutputStream

interface IEncodable {

    fun encode(stream: DataOutputStream)

}