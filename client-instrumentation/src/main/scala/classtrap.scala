package xxx.desu.bzzt
package client

import java.lang.instrument.ClassFileTransformer
import java.lang.ClassLoader
import java.security.ProtectionDomain


object ClassTrap extends ClassFileTransformer {

  def transform ( loader    : ClassLoader
                , className : String
                , clazz     : Class[_]
                , pDomain   : ProtectionDomain
                , classFile : Array[Byte] ) = {

//       println ("snap.")
//       println ("[%s]" format className)
//       println ("[%s]" format loader)
//       println ("[%s]" format veto (loader, className))
      null
    }

  val forbidden = Seq (
      """^javax?\..*"""
    , """^scalax?\..*"""
  )

  def veto (loader : ClassLoader, className: String)
    = (loader == null) || (forbidden exists (className matches _))

}
