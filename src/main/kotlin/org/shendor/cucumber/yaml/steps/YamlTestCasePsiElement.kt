package org.shendor.cucumber.yaml.steps

import com.intellij.lang.ASTNode
import com.intellij.lang.Language
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.pom.PomTargetPsiElement
import com.intellij.psi.*
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import org.shendor.cucumber.yaml.CucumberYamlUtil
import org.shendor.cucumber.yaml.search.YamlStepDeclaration
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLSequenceItem
import org.jetbrains.yaml.psi.YAMLValue
import javax.swing.Icon

class YamlTestCasePsiElement(private val item: YAMLSequenceItem) : YAMLSequenceItem {
    override fun <T : Any?> getUserData(key: Key<T>): T? {
        return item.getUserData(key)
    }

    override fun <T : Any?> putUserData(key: Key<T>, value: T?) {
        item.getUserData(key)
    }

    override fun getIcon(flags: Int): Icon {
        return item.getIcon(flags)
    }

    override fun getProject(): Project {
        return item.project
    }

    override fun getLanguage(): Language {
        return item.language
    }

    override fun getManager(): PsiManager {
        return item.manager
    }

    override fun getChildren(): Array<PsiElement> {
        return item.children
    }

    override fun getParent(): PsiElement {
        return item.parent
    }

    override fun getFirstChild(): PsiElement {
        return item.firstChild
    }

    override fun getLastChild(): PsiElement {
        return item.lastChild
    }

    override fun getNextSibling(): PsiElement {
        return item.nextSibling
    }

    override fun getPrevSibling(): PsiElement {
        return item.prevSibling
    }

    override fun getContainingFile(): PsiFile {
        return item.containingFile
    }

    override fun getTextRange(): TextRange {
        return item.textRange
    }

    override fun getStartOffsetInParent(): Int {
        return item.startOffsetInParent
    }

    override fun getTextLength(): Int {
        return item.textLength
    }

    override fun findElementAt(offset: Int): PsiElement? {
        return item.findElementAt(offset)
    }

    override fun findReferenceAt(offset: Int): PsiReference? {
        return item.findReferenceAt(offset)
    }

    override fun getTextOffset(): Int {
        return item.textOffset
    }

    override fun getText(): String {
        return (CucumberYamlUtil.getStepName(this) + " (of ${item.containingFile.name})")
    }

    override fun textToCharArray(): CharArray {
        return item.textToCharArray()
    }

    override fun getNavigationElement(): PsiElement {
        return item.navigationElement
    }

    override fun getOriginalElement(): PsiElement {
        return item.originalElement
    }

    override fun textMatches(text: CharSequence): Boolean {
        return item.textMatches(text)
    }

    override fun textMatches(element: PsiElement): Boolean {
        return item.textMatches(element)
    }

    override fun textContains(c: Char): Boolean {
        return item.textContains(c)
    }

    override fun accept(visitor: PsiElementVisitor) {
        item.accept(visitor)
    }

    override fun acceptChildren(visitor: PsiElementVisitor) {
        item.acceptChildren(visitor)
    }

    override fun copy(): PsiElement {
        return item.copy()
    }

    override fun add(element: PsiElement): PsiElement {
        return item.add(element)
    }

    override fun addBefore(element: PsiElement, anchor: PsiElement?): PsiElement {
        return item.addBefore(element, anchor)
    }

    override fun addAfter(element: PsiElement, anchor: PsiElement?): PsiElement {
        return item.addAfter(element, anchor)
    }

    override fun checkAdd(element: PsiElement) {
        return item.checkAdd(element)
    }

    override fun addRange(first: PsiElement?, last: PsiElement?): PsiElement {
        return item.addRange(first, last)
    }

    override fun addRangeBefore(first: PsiElement, last: PsiElement, anchor: PsiElement?): PsiElement {
        return item.addRangeBefore(first, last, anchor)
    }

    override fun addRangeAfter(first: PsiElement?, last: PsiElement?, anchor: PsiElement?): PsiElement {
        return item.addRangeAfter(first, last, anchor)
    }

    override fun delete() {
        return item.delete()
    }

    override fun checkDelete() {
        return item.checkDelete()
    }

    override fun deleteChildRange(first: PsiElement?, last: PsiElement?) {
        return item.deleteChildRange(first, last)
    }

    override fun replace(newElement: PsiElement): PsiElement {
        return item.replace(newElement)
    }

    override fun isValid(): Boolean {
        return item.isValid
    }

    override fun isWritable(): Boolean {
        return item.isWritable
    }

    override fun getReference(): PsiReference? {
        return item.reference
    }

    override fun getReferences(): Array<PsiReference> {
        return item.references
    }

    override fun <T : Any?> getCopyableUserData(key: Key<T>): T? {
        return item.getCopyableUserData(key)
    }

    override fun <T : Any?> putCopyableUserData(key: Key<T>, value: T?) {
        return item.putCopyableUserData(key, value)
    }

    override fun processDeclarations(
        processor: PsiScopeProcessor,
        state: ResolveState,
        lastParent: PsiElement?,
        place: PsiElement
    ): Boolean {
        return item.processDeclarations(processor, state, lastParent, place)
    }

    override fun getContext(): PsiElement? {
        return item.context
    }

    override fun isPhysical(): Boolean {
        return item.isPhysical
    }

    override fun getResolveScope(): GlobalSearchScope {
        return item.resolveScope
    }

    override fun getUseScope(): SearchScope {
        return item.useScope
    }

    override fun getNode(): ASTNode {
        return item.node
    }

    override fun isEquivalentTo(another: PsiElement?): Boolean {
        return item.isEquivalentTo(another)
    }

    override fun navigate(requestFocus: Boolean) {
        return item.navigate(requestFocus)
    }

    override fun canNavigate(): Boolean {
        return item.canNavigate()
    }

    override fun canNavigateToSource(): Boolean {
        return item.canNavigateToSource()
    }

    override fun getName(): String? {
        return item.name
    }

    override fun getPresentation(): ItemPresentation? {
        return item.presentation
    }

    override fun getValue(): YAMLValue? {
        return item.value
    }

    override fun getKeysValues(): MutableCollection<YAMLKeyValue> {
        return item.keysValues
    }

    override fun getItemIndex(): Int {
        return item.itemIndex
    }

    override fun equals(other: Any?): Boolean {
        return if (other is YAMLSequenceItem) {
            item.text == other.text
        } else if (other is PomTargetPsiElement && other.target is YamlStepDeclaration) {
            (other.target as YamlStepDeclaration).element == item
        } else item.hashCode() == other.hashCode()
    }

    override fun hashCode(): Int {
        return item.hashCode()
    }
}