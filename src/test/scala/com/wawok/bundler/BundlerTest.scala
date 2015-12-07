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


import org.scalatest._


/**
  * Created by Brian Wawok on 12/7/2015.
  */


class BundlerTest extends FlatSpec with BeforeAndAfterEach {

  var bundler: Bundler = null

  override def beforeEach(): Unit = {
    bundler = new Bundler()
  }

  override def afterEach(): Unit = {
    bundler = null
  }

  val cheese = new Item("Cheese")
  val beef = new Item("Beef")
  val bogohCheese = Bundle(Price("18.00"), ShoppingCart(cheese -> 2))

  "A bundler" should "let us add a single item" in {
    bundler.addItem(cheese, Price("12.00"))
  }

  it should "have an empty cart be free" in {
    val result = bundler.calculateOptimalPrice(ShoppingCart())
    assert(result == Price("0.00"))
  }

  it should "not let us add a bundle that we don't know about" in {
    intercept[IllegalArgumentException] {
      bundler.addBundle(bogohCheese)
    }
  }

  it should "not let us compute a price we do not know about" in {
    intercept[IllegalArgumentException] {
      bundler.calculateOptimalPrice(ShoppingCart(cheese -> 1))
    }
  }

  it should "let us add a single item and compute the price" in {
    bundler.addItem(cheese, Price("12.00"))
    val cart = ShoppingCart(cheese -> 1)
    val result = bundler.calculateOptimalPrice(cart)
    assert(result == Price("12.00"))

  }

  it should "let us add two items with some quantity and compute the prices" in {
    bundler.addItem(cheese, Price("12.00"))
    bundler.addItem(beef, Price("100.00"))
    val cart = ShoppingCart(cheese -> 1, beef -> 2)
    val result = bundler.calculateOptimalPrice(cart)
    assert(result == Price("212.00"))
  }

  it should "not use a bundle that raises price" in {
    bundler.addItem(cheese, Price("12.00"))
    val bogohCheeseExpensive = Bundle(Price("100.00"), ShoppingCart(cheese -> 2))
    bundler.addBundle(bogohCheeseExpensive)
    val cart = ShoppingCart(cheese -> 2)
    val result = bundler.calculateOptimalPrice(cart)
    assert(result == Price("24.00"))
  }

  it should "not be greedy and apply the best bundle" in {
    val cheeseAndBeefCheap = Bundle(Price("5.00"), ShoppingCart(cheese -> 1, beef -> 1))
    bundler.addItem(cheese, Price("12.00"))
    bundler.addItem(beef, Price("10.00"))
    bundler.addBundle(bogohCheese)
    bundler.addBundle(cheeseAndBeefCheap)
    val cart = ShoppingCart(cheese -> 2, beef -> 1)
    val result = bundler.calculateOptimalPrice(cart)
    assert(result == Price("17.00"))
  }

}
