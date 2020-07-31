internal class DefaultStrings : Strings {
    override val example_key: String by lazy { "example string" }
    override val example_key_2: String by lazy { "example string 2" }
    override val description_add_comment: String by lazy { "Add comment" }
    override val user_added_string: String by lazy { "This hasn't been translated yet, but was added during development" }
    override val home_page_sign_in: String by lazy { "Sign in" }
}
internal class FrStrings : Strings {
    override val example_key: String by lazy { "exemple de chaîne" }
    override val example_key_2: String by lazy { "exemple chaîne 2" }
    override val description_add_comment: String by lazy { "Ajouter un commentaire" }
    override val home_page_sign_in: String by lazy { "" }
    override val user_added_string: String by lazy { "" }
}
