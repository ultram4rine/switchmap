package ru.sgu.switchmap.utils

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import scala.concurrent.{ExecutionContext, Future}

class PictureUtil(implicit ec: ExecutionContext) {
  def convertPNGToJPG(filePath: String): Future[Boolean] =
    Future {
      val imgPNG = ImageIO.read(new File(filePath))
      val imgJPG = new BufferedImage(
        imgPNG.getWidth(),
        imgPNG.getHeight(),
        BufferedImage.TYPE_INT_RGB
      )
      imgJPG.createGraphics().drawImage(imgPNG, 0, 0, Color.WHITE, null)

      ImageIO.write(imgJPG, "jpg", new File(filePath.replace(".png", ".jpg")))
    }
}
