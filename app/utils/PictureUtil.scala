package utils

import java.io.File

import javax.imageio.ImageIO
import javax.inject.Singleton

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PictureUtil(implicit ec: ExecutionContext) {
  def convertToJPG(filePath: String): Future[Boolean] =
    Future {
      val img = ImageIO.read(new File(filePath))
      ImageIO.write(img, "jpg", new File(filePath.replace(".png", ".jpg")))
    }
}
