
package study

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class DslTest {
    @ValueSource(strings = ["홍길동", "김철수"])
    @ParameterizedTest
    fun name(name: String) {
        val person =
            introduce {
                name(name)
            }
        person.name shouldBe name
    }

    @Test
    fun company() {
        val person =
            introduce {
                name("홍길동")
                company("다음")
            }
        person.name shouldBe "홍길동"
        person.company shouldBe "다음"
    }

    @Test
    fun skills() {
        val person =
            introduce {
                name("홍길동")
                company("다음")
                skills {
                    soft("A passion for problem solving")
                    soft("Good communication skills")
                    hard("Kotlin")
                }
            }
        person.name shouldBe "홍길동"
        person.company shouldBe "다음"
        person.skills.hard.hard[0] shouldBe "Kotlin"
        person.skills.soft.soft[0] shouldBe "A passion for problem solving"
        person.skills.soft.soft[1] shouldBe "Good communication skills"
    }

    @Test
    fun languages() {
        val person =
            introduce {
                name("홍길동")
                company("다음")
                skills {
                    soft("A passion for problem solving")
                    soft("Good communication skills")
                    hard("Kotlin")
                }
                languages {
                    "Korean" level 5
                    "English" level 3
                }
            }
        person.name shouldBe "홍길동"
        person.company shouldBe "다음"
        person.skills.hard.hard[0] shouldBe "Kotlin"
        person.skills.soft.soft[0] shouldBe "A passion for problem solving"
        person.skills.soft.soft[1] shouldBe "Good communication skills"
        person.levels["Korean"] shouldBe 5
        person.levels["English"] shouldBe 3
    }
}

private fun introduce(block: PersonBuilder.() -> Unit): Person {
    return PersonBuilder().apply(block).build()
}

class Person(
    val name: String = "",
    val company: String = "",
    val skills: Skills = Skills(),
    val levels: MutableMap<String, Int> = mutableMapOf(),
)

class Skills {
    var soft = Soft()
    var hard = Hard()
}

open class Soft() {
    fun add(s: String) {
        soft.add(s)
    }

    var soft: MutableList<String> =
        mutableListOf()
}

open class Hard() {
    fun add(s: String) {
        hard.add(s)
    }

    var hard: MutableList<String> =
        mutableListOf()
}

class PersonBuilder(
    var name: String = "",
    var company: String = "",
    var skills: Skills = Skills(),
    var levels: MutableMap<String, Int> = mutableMapOf(),
) {
    fun name(name: String) {
        this.name = name
    }

    fun company(company: String) {
        this.company = company
    }

    fun skills(function: () -> Unit) {
        function.invoke()
    }

    fun languages(function: () -> Unit) {
        function.invoke()
    }

    fun hard(s: String) {
        this.skills.hard.add(s)
    }

    fun soft(s: String) {
        this.skills.soft.add(s)
    }

    infix fun String.level(other: Int) {
        levels[this] = other
    }

    fun build(): Person = Person(name, company, skills, levels)
}
