package amazonRobots

import amazonRobots.Protocol.Position

/**
 * Created by Swaneet on 19.06.2014.
 */
object AmazonUtils {


  def articles(g:Grid):List[Article] = {
    List(
      Article(5, "Cherry", Position(3, 0)),
      Article(8, "Chocolate", Position(0, 1)),
      Article(150, "Mac", Position(4, 2))
    )
  }

  case class Article(productSize: Int, name: String, productPos: Position)
}
