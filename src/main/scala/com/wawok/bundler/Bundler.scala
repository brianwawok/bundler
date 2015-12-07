/*
 * This software is licensed under the MIT license, quoted below.
 * [https://opensource.org/licenses/MIT]
 *
 * Copyright (c) 2015 Brian Wawok <bwawok@gmail.com>
 *
 *
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 *
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */


package com.wawok.bundler

import java.util.concurrent.locks.ReentrantLock
import com.wawok.bundler.Util._

/**
  * Created by Brian Wawok on 12/7/2015.
  */

class Bundler() {

  @volatile private[this] var itemPrices = Map.empty[Item, Price]

  @volatile private[this] var bundles = List.empty[Bundle]

  private[this] val updateDataLock = new ReentrantLock()


  def addItem(item: Item, price: Price): Unit = {
    withLock(updateDataLock) {
      itemPrices += item -> price
    }
  }


  def addBundle(bundle: Bundle): Unit = {
    withLock(updateDataLock) {
      require(bundle.items.forall(i => itemPrices.contains(i._1)), "A bundle cannot be made for an item we do not know about!")
      bundles +:= bundle
    }
  }

  //no bundles, just a very simple straight price for a cart
  private[this] def calculateStraightPrice(shoppingCart: ShoppingCart): Price = {
    shoppingCart.map {
      case (item, quantity) =>
        itemPrices.getOrElse(item, throw new IllegalArgumentException(s"I am unable to price item $item")) * quantity
    }.sum
  }

  def calculateOptimalPrice(shoppingCart: ShoppingCart): Price = {
    (
      calculateStraightPrice(shoppingCart) +: bundles.filter(bundle => {
        //a bundle is no use for us if we don't have all the needed components
        bundle.items.forall { case (item, quantity) =>
          shoppingCart.getOrElse(item, 0) >= quantity
        }
      }).map(bundle => {
        val updatedCart = bundle.items.foldLeft(shoppingCart) {
          case (subResult, (item, quantity)) => subResult.updated(item, subResult(item) - quantity)
        }
        bundle.price + calculateOptimalPrice(updatedCart)
      })
      ).min
  }


}
