package indigo

enum class Suits(val image: Char) {
    DIAMONDS('♦'),
    HEARTS('♥'),
    SPADES('♠'),
    CLUBS('♣'),
}

enum class Ranks(val image: String, val power: Int) {
    ACE("A", 14),
    DEUCE("2", 2),
    TREY("3", 3),
    FOUR("4", 4),
    FIVE("5", 5),
    SIX("6", 6),
    SEVEN("7", 7),
    EIGHT("8", 8),
    NINE("9", 9),
    TEN("10", 10),
    JACK("J", 11),
    QUEEN("Q", 12),
    KING("K", 13);
}

class Card(private val rank: Ranks, private val suit: Suits) {
    override fun toString(): String {
        return rank.image + suit.image
    }
}

private fun MutableList<Card>.reset() {
    this.clear()
    for (suit in Suits.values())
        for (rank in Ranks.values())
            this.add(Card(rank, suit))
}

private fun MutableList<Card>.getCards() {
    while (true) {
        println("Number of cards:")
        var number = 0
        try {
            number = readln().toInt()
        } catch (ex: Exception) { println("Invalid number of cards."); return }
        if (number !in 1..52) {
            println("Invalid number of cards.")
            return
        }
        if (number > this.size) {
            println("The remaining cards are insufficient to meet the request.")
            return
        }
        println(this.take(number).joinToString(" "))
        this.removeFirst(number)
        //repeat(number) { this.removeAt(0) }
        return
    }
}

/**
 * @param length remove index [0..length)
 */
fun <E> MutableList<E>.removeFirst(length: Int): MutableList<E> {
    if (length in 1..size) {
        subList(0, length).clear()
    }
    return this
}

/**
 * @param length remove index [(size - length)..size)
 */
fun <E> MutableList<E>.removeLast(length: Int): MutableList<E> {
    if (length in 1..size) {
        subList(size - length, size).clear()
    }
    return this
}

fun main() {
    val deck = mutableListOf<Card>()
    deck.reset()
    while (true) {
        println("Choose an action (reset, shuffle, get, exit):")
        when (readlnOrNull() ?: error("No lines read")) {
            "reset" -> { deck.reset(); println("Card deck is reset.") }
            "shuffle" -> { deck.shuffle(); println("Card deck is shuffled.") }
            "get" -> { deck.getCards() }
            "exit" -> { println("Bye"); break }
            else -> println("Wrong action.")
        }
    }
}

