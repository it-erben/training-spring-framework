const { execSync } = require('child_process')
const fs = require('fs')
const path = require('path')

const outputDir = './pdf_slides'

if (!fs.existsSync(outputDir)){
    fs.mkdirSync(outputDir);
}

// Iterate over directories in the root
fs.readdirSync('.').forEach(dir => {
    const dirPath = path.join('.', dir)
    
    // Skip if not a directory or hidden/system files
    if (!fs.statSync(dirPath).isDirectory() || dir.startsWith('.') || dir === 'node_modules' || dir === 'pdf_slides') {
        return;
    }

    const inputPath = path.join(dirPath, 'slides.md')
    
    // Only process if slides.md exists
    if (fs.existsSync(inputPath)) {
        // Output filename based on directory name
        const outputPath = path.join(outputDir, `${dir}.pdf`)
        
        console.log(`Processing ${inputPath} -> ${outputPath}...`)

        try {
            // Run Marp CLI directly on the input file
            // --allow-local-files is crucial for accessing images relative to the markdown file
            execSync(`npx marp "${inputPath}" --pdf --allow-local-files -o "${outputPath}"`, { stdio: 'inherit' })
            console.log(`Created ${outputPath}`)
        } catch (error) {
            console.error(`Failed to process ${dir}:`, error)
        }
    }
})